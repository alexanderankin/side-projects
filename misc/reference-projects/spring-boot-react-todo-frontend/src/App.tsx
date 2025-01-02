import viteLogo from "/vite.svg";
import { useEffect, useState } from "react";
import { Button, Container, Input, ListGroup, ListGroupItem } from "reactstrap";
import reactLogo from "./assets/react.svg";
import { tasksApi } from "./store.tsx";

function HtmlAttributes({ attributes }: { attributes: Record<string, string | false> }) {
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

function App() {
  const [count, setCount] = useState(0);
  const [createTaskTitle, setCreateTaskTitle] = useState("");
  let getTasks = tasksApi.useGetTasksQuery({ page: 0, size: 50 });
  let [createTask, createTaskResult] = tasksApi.useCreateTaskMutation();
  let [deleteTask, deleteTaskResult] = tasksApi.useDeleteTaskByIdMutation();

  function clickCreateTask() {
    createTask({ title: createTaskTitle })
      .then(() => setCreateTaskTitle(""));
  }

  return (
    <div className="container mt-5">
      <HtmlAttributes attributes={{ "data-bs-theme": "dark" }} />
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
                          type="button"
                          className="me-2"
                          disabled={createTaskResult.isLoading}
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

                {createTaskResult.isError
                  ? <div className="alert alert-secondary" role="alert">
                    There was an error: {(createTaskResult.error as Record<string, any>)?.data?.error}
                  </div>
                  : null}

                <div className="">
                  <div className="z-3 w-100 top-0 bottom-0 left-0 right-0">
                    {getTasks.isLoading || getTasks.isFetching
                      ? <>
                        <div className="d-flex justify-content-center align-items-center h-100 mt-3">
                          <div className="spinner-border" role="status">
                            <span className="visually-hidden">Loading...</span>
                          </div>
                        </div>
                      </>
                      : getTasks.data?.content?.length === 0
                        ? <p className="col-md-8 fs-4">
                          No tasks
                        </p>
                        : <>
                          <ListGroup className="pt-2 px-1">
                            {getTasks.data?.content.map(t => (
                              <ListGroupItem key={t.id}>
                                <div className="d-flex w-100 justify-content-between">
                                  <span>
                                    {t.title}
                                  </span>

                                  <div className="d-flex">
                                    <button role="button" className="btn btn-sm btn-success ms-2">
                                      Update
                                    </button>
                                    <button role="button"
                                            className="btn btn-sm btn-danger ms-2"
                                            onClick={() => deleteTask(t)}
                                            disabled={deleteTaskResult.isLoading}
                                    >
                                      Delete
                                    </button>
                                  </div>
                                </div>
                              </ListGroupItem>
                            ))}
                          </ListGroup>
                        </>}
                  </div>
                </div>
              </Container>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
