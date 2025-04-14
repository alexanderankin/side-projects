import { expect } from "chai";
import { container } from "./testSetup.js";

it("adds 1 + 2 to equal 3", () => {
  expect(1 + 2).to.eq(3);
});

it("runs ufw help", async () => {
  let result = await container.exec(["ufw", "-h"])
  console.log(result);
})
