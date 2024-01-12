import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { useMutation, useQuery, useQueryClient } from "react-query";
import { throwIfNotResponseOk } from "./utils.tsx";

function App() {
  let queryClient = useQueryClient();

  const countQuery = useQuery('count', () =>
    fetch('/api/count')
      .then(throwIfNotResponseOk)
      .then(r => r.json())
      .then(t => t.count));

  const updateCounter = useMutation(() =>
    fetch('/api/count/increment', { method: 'PATCH' })
      .then(throwIfNotResponseOk)
      .then(r => r.json())
      .then(b => queryClient.getQueryCache().find('count')?.setData(b.count)))

  return (
    <>
      <div>
        <a href="https://vitejs.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => updateCounter.mutate()}>
          {
            (countQuery.isError || updateCounter.isError)
              ? <>
                Could not load the count:
                <br />
                {String(countQuery.error || updateCounter.error)}
              </>
              : (countQuery.isLoading || updateCounter.isLoading)
                ? <>loading...</>
                : <>count is {countQuery.data}</>
          }
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
