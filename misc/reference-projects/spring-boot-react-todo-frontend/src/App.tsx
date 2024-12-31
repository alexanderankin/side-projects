import viteLogo from "/vite.svg";
import { useEffect, useState } from "react";
import { Button, Container, Input, ListGroup, ListGroupItem } from "reactstrap";
import reactLogo from "./assets/react.svg";

function HtmlAttributes({attributes}: { attributes: Record<string, string | false> }) {
  // when attributes change, update them
  useEffect(() => {
    Object.entries(attributes).forEach(([key, value]) => {
      if (value === false) {
        globalThis?.document?.querySelector("html")?.removeAttribute(key);
      } else {
        globalThis?.document?.querySelector("html")?.setAttribute(key, value);
      }
    });
  }, [attributes]);

  return null;
}

interface Task {
  id?: string;
  title: string;
  description?: string;
}

interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: string;
  totalPages: number;
  totalElements: number;
}

async function createTask(title: string) {
  let response = await fetch("/api/tasks", {
    method: "POST",
    body: JSON.stringify({title}),
    headers: {
      "content-type": "application/json",
    },
  });
  return await response.json() as Promise<Task>;
}

function App() {
  const [count, setCount] = useState(0);
  const [tasks, setTasks] = useState(null as Page<Task> | null);
  const [tasksFetchCounter, setTasksFetchCounter] = useState(0);
  const [, setTasksFetchError] = useState("");
  const [createTaskTitle, setCreateTaskTitle] = useState("");
  const [createStatus, setCreateStatus] = useState("idle");


  useEffect(() => {
    fetch("/api/tasks")
      .then(r => r.json())
      .then(tasks => setTasks(tasks));
  }, [tasksFetchCounter]);

  function clickCreateTask() {
    setCreateStatus("loading");
    createTask(createTaskTitle)
      .then(() => {
        setTasksFetchCounter(t => t + 1);
        setCreateStatus("idle");
      })
      .catch(e => {
        setTasksFetchError(`${e}`);
        setCreateStatus("error");
      });

  }

  return (
    <div className="container mt-5">
      <HtmlAttributes attributes={{"data-bs-theme": "dark"}} />
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card p-2 mb-2">
        <button className="btn btn-secondary" onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p className="mb-0">
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>

      <div className="container mt-3 mt-sm-5">
        <div className="row">
          <div className="col">
            <div className="p-2 p-sm-5 mb-4 bg-secondary rounded-3">
              <Container fluid className="py-3 py-sm-5">
                <h1 className="display-5 fw-bold">Tasks To Do:</h1>

                <div className="d-flex">
                  <Button color="light"
                          size="lg"
                          type="button"
                          className="me-2"
                          disabled={createStatus !== "idle"}
                          onClick={clickCreateTask}>
                    Create
                  </Button>
                  <form onSubmit={e => {
                    e.preventDefault();
                    clickCreateTask();
                  }}>
                    <Input type="text" value={createTaskTitle}
                           onChange={e => setCreateTaskTitle(e.target.value)}>
                    </Input>
                  </form>
                </div>

                {!tasks?.content?.length
                  ? <p className="col-md-8 fs-4">
                    There are no tasks yet!
                  </p>
                  : <ListGroup className="pt-2 px-1">
                    {tasks.content.map(t => (
                      <ListGroupItem key={t.id}>{t.title}</ListGroupItem>
                    ))}
                  </ListGroup>}
              </Container>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
