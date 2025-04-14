import { expect } from "chai";
import { container } from "../../test/testSetup.js";
import { UfwManager } from "./ufw-manager.js";

/**
 * ok so here is where I realized that i actually need the component that does the UFW calls to be separated from the API component with a network boundary
 * <p>
 * because the development machine is not guaranteed to be linux, I need to actually talk to it over a network not just do cli calls.
 * <p>
 * otherwise all i can do is cli calls, and the part of the testing that is the containerization becomes part of the stack.
 * <p>
 * the thing i wanted to do was talk to bare metal in prod, and containers in test
 * <p>
 * so i just need to replace this with a network boundary.
 * <p>
 * maybe this project requires some better langauge....
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
