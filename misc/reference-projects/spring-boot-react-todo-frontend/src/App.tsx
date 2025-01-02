import viteLogo from "/vite.svg";
import { FC, PropsWithChildren, useEffect, useRef, useState } from "react";
import { Button, Input, ListGroup, ListGroupItem } from "reactstrap";
import reactLogo from "./assets/react.svg";
import { tasksApi, tasksUi, useAppDispatch, useAppSelector } from "./store.tsx";

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

function Spinner() {
  return <div className="d-flex justify-content-center align-items-center h-100 mt-3">
    <div className="spinner-border" role="status">
      <span className="visually-hidden">Loading...</span>
    </div>
  </div>;
}

function TaskList() {
  const editedTasks = useAppSelector(s => s.tasksUi.tasks);
  let appDispatch = useAppDispatch();
  const getTasks = tasksApi.useGetTasksQuery({ page: 0, size: 100 });
  const [deleteTask, deleteTaskResult] = tasksApi.useDeleteTaskByIdMutation();
  const [updateTask, updateTaskResult] = tasksApi.useUpdateTaskByIdMutation();
  let inputRef = useRef<(HTMLInputElement | null)[]>([]);

  return <ListGroup className="pt-2 px-1">
    {getTasks.data?.content.map((t, index) => {
      let editedTask = editedTasks[t.id] || { editing: false };
      let editing = editedTask.editing;

      return (
        <ListGroupItem key={t.id}>
          <div className="d-flex w-100 justify-content-between">
            <form className={editing ? "" : "d-none"} onSubmit={e => {
              e.preventDefault();
              updateTask(editedTask.newData)
                .then((t) => {
                  t.data && appDispatch(tasksUi.actions.stopEditingPost(t.data.id));
                });
            }}>
              <input
                ref={el => inputRef.current[index] = el}
                disabled={updateTaskResult.isLoading}
                value={editedTask.newData?.title || ""}
                onBlur={() => appDispatch(tasksUi.actions.stopEditingPost(t.id))}
                onKeyDown={e => !(e.key === "Escape" && appDispatch(tasksUi.actions.stopEditingPost(t.id)))}
                onChange={e => appDispatch(tasksUi.actions.edit({ ...t, title: e.target.value }))}
              >
              </input>
            </form>
            <span className={editing ? "d-none" : ""}>
              {t.title}
            </span>

            <div className="d-flex">
              <button role="button"
                      className="btn btn-sm btn-success ms-2"
                      onClick={() => {
                        appDispatch(tasksUi.actions.startEditingPost(t));
                        setTimeout(() => {
                          inputRef.current[index]!.focus();
                        });
                      }}
              >
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
      );
    })}
  </ListGroup>;
}

function App() {
  const [count, setCount] = useState(0);
  const [createTaskTitle, setCreateTaskTitle] = useState("");
  let getTasks = tasksApi.useGetTasksQuery({ page: 0, size: 100 });
  let [createTask, createTaskResult] = tasksApi.useCreateTaskMutation();

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

      <CustomJumboTron>
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
            <Input
              type="text"
              value={createTaskTitle}
              onChange={e => setCreateTaskTitle(e.target.value)}
            >
            </Input>
          </form>
        </div>

        {createTaskResult.isError
          ? <div className="alert alert-secondary" role="alert">
            There was an error: {(createTaskResult.error as Record<string, any>)?.data?.error}
          </div>
          : null}

        <div>
          <div className="z-3 w-100 top-0 bottom-0 left-0 right-0">
            {getTasks.isLoading || getTasks.isFetching
              ? <Spinner />
              : getTasks.data?.content?.length === 0
                ? (
                  <p className="col-md-8 fs-4">No tasks</p>
                )
                : (
                  <TaskList />
                )
            }
          </div>
        </div>
      </CustomJumboTron>
    </div>
  );
}

const CustomJumboTron: FC<PropsWithChildren> = function ({ children }) {
  return <>
    <div className="container mt-3 mt-sm-5">
      <div className="row">
        <div className="col">
          <div className="p-2 p-sm-5 mb-4 bg-secondary rounded-3">
            <div className="container-fluid py-3 py-sm-5">
              {children}
            </div>
          </div>
        </div>
      </div>
    </div>
  </>;
};

export default App;
