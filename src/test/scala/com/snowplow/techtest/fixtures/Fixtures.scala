package com.snowplow.techtest.fixtures
import io.circe._
import io.circe.parser._

trait Fixtures {

  val configSchema: String = """{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "properties": {
      "source": {
      "type": "string"
    },
      "destination": {
      "type": "string"
    },
      "timeout": {
      "type": "integer",
      "minimum": 0,
      "maximum": 32767
    },
      "chunks": {
      "type": "object",
      "properties": {
      "size": {
      "type": "integer"
    },
      "number": {
      "type": "integer"
    }
    },
      "required": ["size"]
    }
    },
    "required": ["source", "destination"]
  }""".strip

  val inputNoNullValues: String = """{
    "source": "source_value",
    "destination": "destination_value",
    "timeout": 1,
    "chunks": {
      "size": 1,
      "number": 1
    }
  }""".strip

  val inputWithoutNonRequiredFields: String = """{
    "source": "source_value",
    "destination": "destination_value"
  }""".strip

  val inputToValidate: String = """{
    "source": "/home/alice/image.iso",
    "destination": "/mnt/storage",
    "timeout": null,
    "chunks": {
      "size": 1024,
      "number": null
    }
  }""".strip

  val inputWithNullFields: String = """{
    "source": "/home/alice/image.iso",
    "destination": "/mnt/storage",
    "timeout": null,
    "chunks": null
  }""".strip

  val configSchemaJson: Json                       = parse(configSchema).right.get
  val jsonToValidate: Json                         = parse(inputToValidate).right.get
  val jsonToValidateNoNullValues: Json             = parse(inputNoNullValues).right.get
  val jsonToValidateWithoutNonRequiredFields: Json = parse(inputWithoutNonRequiredFields).right.get
  val jsonToValidateWithNullFields: Json           = parse(inputWithNullFields).right.get
  val schemaId                                     = "config-schema.json"
}
