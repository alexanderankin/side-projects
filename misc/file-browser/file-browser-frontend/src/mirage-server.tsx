let fakeServer = () => {};

// @ts-ignore
if (!window['notMirage']) {
  // @ts-ignore
  const { createServer, Response } = await import("miragejs")

  fakeServer = function() {
    // noinspection JSUnusedGlobalSymbols
    createServer({
      routes() {
        let count = 0;
        this.get("/api/count", () => ({
          count,
        }))
        this.patch("/api/count/increment", () => {
          count++
          if (count % 10 === 0) return new Response(400, undefined, 'um ok');
          return { count }
        })
      },
    })
  }
}

export default fakeServer;
