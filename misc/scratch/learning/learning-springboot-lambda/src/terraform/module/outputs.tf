output "frontend_url" {
  value = aws_cloudfront_distribution.frontend.domain_name
}

output "api_url" {
  value = aws_apigatewayv2_api.http.api_endpoint
}
