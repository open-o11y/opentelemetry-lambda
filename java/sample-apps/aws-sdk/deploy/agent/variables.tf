variable "name" {
  type        = string
  description = "Name of created function and API Gateway"
  default     = "hello-java-awssdk-agent"
}

variable "collector_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry collector extension"
  default = "arn:aws:lambda:us-east-1:614732350472:layer:otel-collector-with-awsprw:3"
  // TODO(anuraaga): Add default when a public layer is published.
}

variable "sdk_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry Java Agent"
  default = "arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-java-agent-ver-1-2-0:2"
  // TODO(anuraaga): Add default when a public layer is published.
}

variable "tracing_mode" {
  type        = string
  description = "Lambda function tracing mode"
  default     = "PassThrough"
}
