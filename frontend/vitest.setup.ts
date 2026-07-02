import "@testing-library/jest-dom";

// jsdom não implementa URL.createObjectURL; stub para os testes.
if (typeof URL.createObjectURL !== "function") {
  URL.createObjectURL = () => "blob:mock";
}
