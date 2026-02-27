package info.ankin.projects.jsonschema.model.v4;

import info.ankin.projects.jsonschema.model.annotation.Format;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @see <a href="https://github.com/json-schema-org/json-schema-spec/blob/dba92b702c94858162f653590230e7573c8b7dd0/schema.json">V4 schema Schema</a>
 */
@Data
public class Schema {
    private String id;
    private String $schema;
    private String title;
    private String description;

    // "default": {},

    @Positive
    private int multipleOf;
    private int maximum;
    private boolean exclusiveMaximum;
    private int minimum;
    private boolean exclusiveMinimum;
    // "maxLength": { "$ref": "#/definitions/positiveInteger" },
    // "minLength": { "$ref": "#/definitions/positiveIntegerDefault0" },

    @Format("regex")
    private String pattern;
    /*
    "additionalItems": {
        "anyOf": [
            { "type": "boolean" },
            { "$ref": "#" }
        ],
        "default": {}
    },
    "items": {
        "anyOf": [
            { "$ref": "#" },
            { "$ref": "#/definitions/schemaArray" }
        ],
        "default": {}
    },
    "maxItems": { "$ref": "#/definitions/positiveInteger" },
    "minItems": { "$ref": "#/definitions/positiveIntegerDefault0" },
    "uniqueItems": {
        "type": "boolean",
        "default": false
    },
    "maxProperties": { "$ref": "#/definitions/positiveInteger" },
    "minProperties": { "$ref": "#/definitions/positiveIntegerDefault0" },
    "required": { "$ref": "#/definitions/stringArray" },
    "additionalProperties": {
        "anyOf": [
            { "type": "boolean" },
            { "$ref": "#" }
        ],
        "default": {}
    },
    "definitions": {
        "type": "object",
        "additionalProperties": { "$ref": "#" },
        "default": {}
    },
    "properties": {
        "type": "object",
        "additionalProperties": { "$ref": "#" },
        "default": {}
    },
    "patternProperties": {
        "type": "object",
        "additionalProperties": { "$ref": "#" },
        "default": {}
    },
    "dependencies": {
        "type": "object",
        "additionalProperties": {
            "anyOf": [
                { "$ref": "#" },
                { "$ref": "#/definitions/stringArray" }
            ]
        }
    },
    "enum": {
        "type": "array",
        "minItems": 1,
        "uniqueItems": true
    },
    "type": {
        "anyOf": [
            { "$ref": "#/definitions/simpleTypes" },
            {
                "type": "array",
                "items": { "$ref": "#/definitions/simpleTypes" },
                "minItems": 1,
                    "uniqueItems": true
            }
        ]
    },
    "format": { "type": "string" },
    "allOf": { "$ref": "#/definitions/schemaArray" },
    "anyOf": { "$ref": "#/definitions/schemaArray" },
    "oneOf": { "$ref": "#/definitions/schemaArray" },
    "not": { "$ref": "#" }
    */
}
