resource "time_rotating" "tr" {
  rotation_minutes = 1
}

data "http" "test" {
  url             = "http://localhost:8080"
  method          = "POST"
  request_headers = { "content-type" : "application/json" }
  request_body    = jsonencode({
    time_rotating = time_rotating.tr
    now           = timestamp()
  })


}

output "output" {
  value = data.http.test
}
