import { expect } from "chai";
import { container } from "../../test/testSetup.js";
import { UfwManager } from "./ufw-manager.js";

/**
 * AAAAAhhhhhh!!!!!
 */
describe("UfwManager", () => {
  describe("initialization", () => {
    it("should", async () => {
      let result = await container.exec("ufw status");
      if (result.output?.startsWith("Status: inactive")) {
        result = await container.exec("ufw enable");
        expect(result.exitCode).to.equal(0);
      }


    });
  });

  describe("rules", () => {
    // it("should list rules", async () => {
    //   await container.exec("ufw status")
    // })
  });
});
