import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { loadEnvFile } from "node:process";
import { fileURLToPath } from "node:url";


let __filename = fileURLToPath(import.meta.url);
let __dirname = dirname(__filename);

if (existsSync(join(__dirname, ".env")))
  loadEnvFile(join(__dirname, ".env"));

let stateFile = process.env["STATE_FILE"];

/**
 * @type {{
 *   version: string
 *   terraform_version: string
 *   serial: number
 *   lineage: string
 *   outputs: Record<string, {
 *     value: any,
 *     type: [string, any]
 *   }>
 *   resources: {
 *     module: string
 *     mode: "managed" | "data"
 *     type: string
 *     name: string
 *     provider: string
 *     instances: {
 *       index_key?: string | number
 *       schema_version: number
 *       attributes: Record<string, any>
 *     }[]
 *   }[]
 * }}
 */
let originalState = JSON.parse(readFileSync(stateFile, "utf8"));

let state = structuredClone(originalState);

state.resources = state.resources.filter(r => r.mode === "managed");

if (!!process.env["MODULE_FILTER_PREFIX"])
  state.resources = state.resources.filter(r => r.module.startsWith(process.env["MODULE_FILTER_PREFIX"]));

if (!!process.env["MODULE_REPLACEMENTS"]) {
  /**
   * @type string[]
   */
  let replacements = JSON.parse(process.env["MODULE_REPLACEMENTS"])
  for (let modReplace of replacements) {
    const re = /^s\/([^/]+)\/([^/]+)\//;
    const match = modReplace.match(re);

    if (!match) {
      throw new Error("Invalid format. Expected s/pattern/replacement/ but got: " + modReplace);
    }

    const [, pattern, replacement] = match;
    // noinspection JSValidateTypes
    state.resources = state.resources.map(r => ({ ...r, module: r.module.replace(pattern, replacement) }));
  }
}

let instances = state.resources
  .flatMap(resource => resource.instances.map(instance => ({ resource, instance })));

/*
console.log(instances.reduce((acc, next) => {
  acc[next.resource.type] = acc[next.resource.type] || 0;
  acc[next.resource.type] += 1;
  return acc;
}, {}));
*/

let imports = instances.map(instance => {
  let resourcePath = instance.resource.module + "." + instance.resource.type + "." + instance.resource.name;
  if ("index_key" in instance.instance) {
    if (typeof instance.instance.index_key === "number")
      resourcePath += "[" + instance.instance.index_key + "]";
    else
      resourcePath += "[\"" + instance.instance.index_key + "\"]";
  }
  let importStatement = {
    id: instance.instance.attributes.id,
    to: resourcePath,
  };

  let resourceType = instance.resource.type.substring("aws_".length);
  switch (resourceType) {
    case "flow_log": {
      break;
    }
    case "internet_gateway": {
      break;
    }
    case "route": {
      // if (!!instance.instance.attributes.gateway_id)
      //   importStatement.id = instance.instance.attributes.route_table_id + "_" + instance.instance.attributes.gateway_id;
      // if (!!instance.instance.attributes.network_interface_id)
      //   importStatement.id = instance.instance.attributes.route_table_id + "_" + instance.instance.attributes.network_interface_id;
      if (!!instance.instance.attributes.destination_cidr_block)
        importStatement.id = instance.instance.attributes.route_table_id + "_" + instance.instance.attributes.destination_cidr_block;
      else if (!!instance.instance.attributes.destination_ipv6_cidr_block)
        importStatement.id = instance.instance.attributes.route_table_id + "_" + instance.instance.attributes.destination_ipv6_cidr_block;
      // else if (!!instance.instance.attributes.network_interface_id)
      //   importStatement.id = instance.instance.attributes.route_table_id + "_" + instance.instance.attributes.network_interface_id;
      else
        throw new Error("not supported: " + JSON.stringify(instance.instance));
      break;
    }
    case "route53_zone": {
      break;
    }
    case "route_table": {
      break;
    }
    case "route_table_association": {
      if (!!instance.instance.attributes.gateway_id)
        importStatement.id = instance.instance.attributes.gateway_id + "/" + instance.instance.attributes.route_table_id;
      else if (!!instance.instance.attributes.subnet_id)
        importStatement.id = instance.instance.attributes.subnet_id + "/" + instance.instance.attributes.route_table_id;
      else
        throw new Error(JSON.stringify(instance.instance));
      break;
    }
    case "security_group": {
      break;
    }
    case "security_group_rule": {
      let {
        security_group_id,
        type,
        protocol,
        from_port,
        to_port,
        cidr_blocks,
        ipv6_cidr_blocks,
        prefix_list_ids,
        source_security_group_id,
      } = instance.instance.attributes;

      let idPrefix = security_group_id + "_" + type + "_" + protocol + "_" + from_port + "_" + to_port + "_";
      if (cidr_blocks?.length)
        importStatement.id = idPrefix + cidr_blocks.join("_");
      else if (ipv6_cidr_blocks?.length)
        importStatement.id = idPrefix + ipv6_cidr_blocks.join("_");
      else if (prefix_list_ids?.length)
        importStatement.id = idPrefix + prefix_list_ids.join("_");
      else if (!!source_security_group_id)
        importStatement.id = idPrefix + source_security_group_id;
      else
        throw new Error("not supported: " + JSON.stringify(instance.instance.attributes));
      break;
    }
    case "subnet": {
      break;
    }
    case "vpc": {
      break;
    }
    case "wafv2_ip_set": {
      let { id, name, scope } = instance.instance.attributes;
      importStatement.id = [id, name, scope].join("/");
      break;
    }
    case "eip": {
      break;
    }
    case "instance": {
      break;
    }
    case "network_interface": {
      break;
    }
    case "nat_gateway": {
      break;
    }
  }

  if (!importStatement.id)
    throw new Error("not supported type: " + instance.resource.type);

  return {
    instance,
    importStatement,
  };
});

console.log(imports.map(({ importStatement }) => `import {
  id = "${importStatement.id}"
  to = ${importStatement.to}
}`).join("\n"));
