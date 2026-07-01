
import torch
import torch.nn as nn
import torch.nn.functional as F

try:
    from torchvision.ops import DeformConv2d
    HAS_DEFORM_CONV = True
except ImportError:
    HAS_DEFORM_CONV = False


class SEBlock(nn.Module):
    """Squeeze-and-Excitation para atencao por canal."""

    def __init__(self, channels, reduction=8):
        super().__init__()
        self.squeeze = nn.AdaptiveAvgPool2d(1)
        self.excitation = nn.Sequential(
            nn.Conv2d(channels, channels // reduction, 1, bias=False),
            nn.PReLU(),
            nn.Conv2d(channels // reduction, channels, 1, bias=False),
            nn.Sigmoid(),
        )

    def forward(self, x):
        scale = self.excitation(self.squeeze(x))
        return x * scale


class ResidualBlock(nn.Module):
    """Bloco residual leve com SE-attention opcional (refino no espaco LR)."""

    def __init__(self, channels, use_attention=True):
        super().__init__()
        self.body = nn.Sequential(
            nn.Conv2d(channels, channels, 3, padding=1),
            nn.PReLU(),
            nn.Conv2d(channels, channels, 3, padding=1),
        )
        self.attention = SEBlock(channels) if use_attention else nn.Identity()

    def forward(self, x):
        return x + self.attention(self.body(x))


class ConvGRU(nn.Module):
    """Unidade recorrente convolucional para fusao temporal."""

    def __init__(self, hidden_dim):
        super().__init__()
        self.conv_reset = nn.Conv2d(hidden_dim * 2, hidden_dim, 3, padding=1)
        self.conv_update = nn.Conv2d(hidden_dim * 2, hidden_dim, 3, padding=1)
        self.conv_candidate = nn.Conv2d(hidden_dim * 2, hidden_dim, 3, padding=1)

    def forward(self, feat, prev_state):
        combined = torch.cat([feat, prev_state], dim=1)
        reset = torch.sigmoid(self.conv_reset(combined))
        update = torch.sigmoid(self.conv_update(combined))
        candidate = torch.tanh(
            self.conv_candidate(torch.cat([feat, reset * prev_state], dim=1))
        )
        new_state = (1 - update) * prev_state + update * candidate
        return new_state


class _DeformableAligner(nn.Module):
    """Alinhamento temporal via convolucao deformavel."""

    def __init__(self, hidden_dim):
        super().__init__()
        self.offset_conv = nn.Sequential(
            nn.Conv2d(hidden_dim * 2, hidden_dim, 3, padding=1),
            nn.PReLU(),
            nn.Conv2d(hidden_dim, 2 * 3 * 3, 3, padding=1),
        )
        self.deform_conv = DeformConv2d(hidden_dim, hidden_dim, 3, padding=1)

    def forward(self, feat, prev_state):
        offsets = self.offset_conv(torch.cat([feat, prev_state], dim=1))
        return self.deform_conv(prev_state, offsets)


class _ConvAligner(nn.Module):
    """Fallback: alinhamento via convolucao padrao quando DeformConv2d ausente."""

    def __init__(self, hidden_dim):
        super().__init__()
        self.align = nn.Sequential(
            nn.Conv2d(hidden_dim * 2, hidden_dim, 3, padding=1),
            nn.PReLU(),
            nn.Conv2d(hidden_dim, hidden_dim, 3, padding=1),
        )

    def forward(self, feat, prev_state):
        return self.align(torch.cat([feat, prev_state], dim=1))


class RTDVSR(nn.Module):
    """Real-Time Deformable Video Super-Resolution (uso em imagem unica)."""

    def __init__(self, scale_factor=2, channels=3, hidden_dim=64, num_res_blocks=4):
        super().__init__()
        self.scale_factor = scale_factor
        self.hidden_dim = hidden_dim
        self.num_res_blocks = num_res_blocks

        self.feat_extract = nn.Sequential(
            nn.Conv2d(channels, hidden_dim, 5, padding=2),
            nn.PReLU(),
            nn.Conv2d(hidden_dim, hidden_dim, 3, padding=1),
            nn.PReLU(),
            nn.Conv2d(hidden_dim, hidden_dim, 3, padding=1),
            nn.PReLU(),
        )

        if HAS_DEFORM_CONV:
            self.aligner = _DeformableAligner(hidden_dim)
        else:
            self.aligner = _ConvAligner(hidden_dim)

        self.fusion = ConvGRU(hidden_dim)

        self.refine = nn.Sequential(
            *[ResidualBlock(hidden_dim, use_attention=(i % 2 == 1))
              for i in range(num_res_blocks)]
        )

        self.upsample = nn.Sequential(
            nn.Conv2d(hidden_dim, channels * (scale_factor ** 2), 3, padding=1),
            nn.PixelShuffle(scale_factor),
        )

    def forward(self, x, prev_state=None):
        feat = self.feat_extract(x)

        if prev_state is None:
            prev_state = torch.zeros_like(feat)

        aligned = self.aligner(feat, prev_state)
        state = self.fusion(feat, aligned)
        refined = self.refine(state) + feat

        residual = self.upsample(refined)
        base = F.interpolate(x, scale_factor=self.scale_factor,
                             mode="bicubic", align_corners=False)
        sr = residual + base

        return sr, state
