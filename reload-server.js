const http = require("http")

const server = http.createServer((req, res) => {
  res.writeHead(200, { "Content-Type": "text/plain" })
  res.end("Ok")

  require("child_process").exec("bash ./autopacker.sh", (err, stdout) => {
    console.log(stdout)
  })
})

server.listen(8080, () => {
  console.log("Send any request to http://localhost:8080 to reload jar library")
})
