variable "name" {
  type        = string
  description = "Name of created function and API Gateway"
  default     = "hello-java-awssdk-agent"
}

variable "collector_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry collector extension"
  // TODO(anuraaga): Add default when a public layer is published.
}


variable "collector_config_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry collector configuration file"
  // TODO(anuraaga): Add default when a public layer is published.
  default = "arn:aws:lambda:us-east-1:614732350472:layer:CustomCollectorConfig:1"
}


variable "sdk_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry Java Agent"
  // TODO(anuraaga): Add default when a public layer is published.
}

variable "tracing_mode" {
  type        = string
  description = "Lambda function tracing mode"
  default     = "PassThrough"
}
