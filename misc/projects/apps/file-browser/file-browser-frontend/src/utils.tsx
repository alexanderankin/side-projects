export async function throwIfNotResponseOk(r: Response): Promise<Response> {
  await throwIfNot(() => r.ok, async () => 'response was not okay: ' + await r.text());
  return r;
}

export async function throwIfNot(fn: () => boolean, msg: () => Promise<string>) {
  if (!fn()) {
    throw new Error(await msg());
  }
}
