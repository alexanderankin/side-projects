import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import classNames from "classnames";
import { produce } from "immer";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Outlet, useNavigate, useParams } from "react-router-dom";
import {
  type CategoryType,
  type NewGameType,
  type NewQuizType,
  type QuizType,
  Worths,
  type WorthType,
} from "./models.ts";
import { apiService } from "./services.tsx";

export function App() {
  const [navbarNavCollapsed, setNavbarNavCollapsed] = useState(true);
  let collapseNavBar = setNavbarNavCollapsed.bind(null, true);

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
        <div className="container-fluid">
          <Link className="navbar-brand" to="/">Quiz</Link>
          <button
            className="navbar-toggler"
            type="button"
            onClick={() => setNavbarNavCollapsed((s) => !s)}
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div className={classNames({ "navbar-collapse": true, collapse: navbarNavCollapsed })}>
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <Link className="nav-link" to="/create" onClick={collapseNavBar}>Create</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/play" onClick={collapseNavBar}>Play</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/settings" onClick={collapseNavBar}>Settings</Link>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <div className="sidebar">
        <ul className="nav flex-column">
          <li className="nav-item">
            <Link className="nav-link" to="/create" onClick={collapseNavBar}>Create</Link>
          </li>
          <li className="nav-item">
            <Link className="nav-link" to="/play" onClick={collapseNavBar}>Play</Link>
          </li>
          <li className="nav-item">
            <Link className="nav-link" to="/settings" onClick={collapseNavBar}>Settings</Link>
          </li>
        </ul>
      </div>

      <div className="main-content">
        <Outlet />
      </div>
    </>
  );
}

export function Welcome() {
  return <>
    <h1>Welcome</h1>
    <p>Welcome</p>
  </>;
}

export function Settings() {
  return <>
    <h1>Settings</h1>
    <p>Settings</p>
  </>;
}

/*
export function Create() {
  return <>
    <h1>Create</h1>
    <p>
      Use this screen to create a custom game with your questions.
    </p>
  </>
}
*/

export function Create() {
  const { register, handleSubmit, formState: { errors }, reset } = useForm<NewQuizType>();

  const [serverError, setServerError] = useState<string | null>(null);
  const clearServerError = setServerError.bind(null, null);

  const createMutation = useMutation({
    mutationFn: async (data: NewQuizType) => {
      setServerError(null);
      return apiService.createQuiz(data);
    },
    onSuccess: (quiz) => {
      reset();
      queryClient.invalidateQueries({ queryKey: ["quizzes"] }).then();
    },
    onError: (err: any) => {
      setServerError(err.message ?? "Unknown error");
    },
  });

  const listQuizzesQuery = useQuery({
    queryKey: ["quizzes"],
    queryFn: () => apiService.listQuizzes(),
  });

  const navigate = useNavigate();

  const queryClient = useQueryClient();
  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiService.deleteQuiz(id);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["quizzes"] }),
  });

  const fetching = createMutation.isPending || listQuizzesQuery.isFetching || deleteMutation.isPending;

  return (
    <>
      <h1>Create</h1>
      <p>Use this screen to create a custom game with your questions.</p>

      <div style={{ maxWidth: 400 }}>
        {serverError && (
          <div className="alert alert-danger alert-dismissible" role="alert">
            {serverError}
            <button type="button" className="btn-close" aria-label="Close" onClick={clearServerError}></button>
          </div>
        )}

        <form onSubmit={handleSubmit((data) => createMutation.mutate(data))} className="mt-3" noValidate>
          <div className="mb-3">
            <label htmlFor="name" className="form-label">Name</label>
            <input
              id="name"
              type="text"
              className={classNames({ "form-control": true, "is-invalid": errors.name })}
              placeholder="Enter quiz name"
              autoComplete="off"
              {...register("name", {
                required: "Name is required",
                minLength: {
                  value: 3,
                  message: "Name must be at least 3 characters",
                },
              })}
            />
            {errors.name && (<div className="invalid-feedback">{errors.name.message}</div>)}
          </div>

          <button type="submit" className={classNames("btn btn-primary", { disabled: createMutation.isPending })}>
            {createMutation.isPending ? "Creating..." : "Create"}
          </button>
        </form>
      </div>

      <hr className="my-4" />

      <h2>Existing Quizzes</h2>
      {
        listQuizzesQuery.isFetching ? <p>Loading...</p> : (
          listQuizzesQuery.data === undefined || listQuizzesQuery.data.length === 0 ? (
            <p>No quizzes yet.</p>
          ) : (
            <ul className="list-group">
              {listQuizzesQuery.data.map((q) => (
                <li key={q.id} className="list-group-item d-flex justify-content-between align-items-center">
                  <span>{q.name}</span>
                  <div className="btn-group">
                    <button
                      className={classNames("btn btn-sm btn-outline-primary", { disabled: fetching })}
                      onClick={() => navigate(`/create/${q.id}`)}
                    >
                      Edit
                    </button>
                    <button
                      className={classNames("btn btn-sm btn-outline-danger", { disabled: fetching })}
                      onClick={() => deleteMutation.mutate(q.id)}
                    >
                      Delete
                    </button>
                    <button
                      className={classNames("btn btn-sm btn-outline-success", { disabled: fetching })}
                      onClick={() => navigate(`/launch/${q.id}`)}
                    >
                      Launch
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )
        )
      }
    </>
  );
}

export function Edit() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [quiz, setQuiz] = useState<QuizType | null>(null);

  useEffect(() => {
    apiService.getQuiz(id!).then(setQuiz).finally(() => setLoading(false));
  }, [id]);

  const [serverError, setServerError] = useState<string | null>(null);
  const clearServerError = setServerError.bind(null, null);

  let queryClient = useQueryClient();
  const saveMutation = useMutation({
    mutationFn: async () => {
      setServerError(null);
      return apiService.updateQuiz(id!, quiz!);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["quizzes"] }).then();
    },
    onError: (err: any) => {
      setServerError(err.message ?? "Unknown error");
    },
  });

  if (loading) return <p>Loading...</p>;
  if (!quiz) return <p>No quiz found.</p>;

  const moveCategory = (from: number, to: number) => setQuiz(q => produce(q, d => {
    const cats = d!.categories;
    [cats[from], cats[to]] = [cats[to], cats[from]];
    cats.forEach((c, i) => c.ordinal = i);
  }));
  const deleteCategory = (index: number) => setQuiz(q => produce(q, d => {
    d!.categories.splice(index, 1);
    d!.categories.forEach((c, i) => c.ordinal = i);
  }));
  const addCategory = (name: string) => setQuiz(q => produce(q, d => {
    d!.categories.push({ name, ordinal: d!.categories.length, questions: [] });
  }));

  return <>
    <h1>Editing {quiz.name}</h1>
    <hr />
    {serverError && (
      <>
        <div className="alert alert-danger alert-dismissible" role="alert">
          {serverError}
          <button type="button" className="btn-close" aria-label="Close" onClick={clearServerError}></button>
        </div>
        <hr />
      </>
    )}
    <button className={classNames("btn btn-success", { disabled: saveMutation.isPending })} onClick={() => {
      saveMutation.mutate();
    }}>
      {!saveMutation.isPending ? "Save" : "Saving..."}
    </button>
    <hr />
    <div className="container-fluid">
      <div style={{ overflowX: "auto" }}>
        <div className="row flex-nowrap">
          {quiz.categories.map((c, _, all) => (
            <div key={c.name} className="col-6 col-sm-4 col-md-2">
              <EditCategoryColumn
                category={c}
                total={all.length}
                onMoveLeft={() => moveCategory(c.ordinal, c.ordinal - 1)}
                onMoveRight={() => moveCategory(c.ordinal, c.ordinal + 1)}
                onDelete={() => deleteCategory(c.ordinal)}
                onUpdate={updater =>
                  setQuiz(q => produce(q, d => {
                    updater(d!.categories[c.ordinal]);
                  }))
                }
              />
            </div>
          ))}
          <div className="col-6 col-sm-4 col-md-2">
            <EditAddCategoryForm existing={quiz.categories.map(c => c.name)} onAdd={addCategory} />
          </div>
        </div>
      </div>
    </div>
  </>;
}

function notExistsInCategoryFilter(category: CategoryType) {
  return function (worth: number): boolean {
    for (let question of category.questions) {
      if (question.worth === worth) {
        return false;
      }
    }
    return true;
  };
}

function EditCategoryColumn({ category, total, onMoveLeft, onMoveRight, onDelete, onUpdate }: {
  category: CategoryType,
  total: number,
  onMoveLeft: () => void,
  onMoveRight: () => void,
  onDelete: () => void
  onUpdate: (updater: (draft: CategoryType) => void) => void
}) {
  return <>
    <p>Category {category.ordinal + 1}: <strong>{category.name}</strong></p>
    <div className="mt-2 mb-2 d-flex justify-content-center gap-2">
      <button onClick={onMoveLeft}
              className={classNames("btn btn-sm btn-outline-success", { disabled: category.ordinal === 0 })}>Left
      </button>
      <button onClick={onMoveRight}
              className={classNames("btn btn-sm btn-outline-warning", { disabled: category.ordinal === total - 1 })}>Right
      </button>
    </div>
    <div className="d-flex justify-content-center">
      <button onClick={onDelete} className="btn btn-sm btn-outline-danger">Delete</button>
    </div>

    <div className="border rounded p-2 mb-2 bg-light">
      {category.questions.map((q, i) => (
        <div key={i} className="mb-2 border-bottom pb-1">
          <div><strong>{q.worth}</strong> — {q.question}</div>
          <div className="small text-muted">Answer: {q.answer}</div>
          <div className="d-flex justify-content-center gap-2 mt-1">
            <button onClick={() => onUpdate(d => {
              const t = d.questions[i - 1];
              d.questions[i - 1] = d.questions[i];
              d.questions[i] = t;
            })} className={classNames("btn btn-sm btn-outline-success", { disabled: i === 0 })}>↑
            </button>
            <button onClick={() => onUpdate(d => {
              const t = d.questions[i + 1];
              d.questions[i + 1] = d.questions[i];
              d.questions[i] = t;
            })}
                    className={classNames("btn btn-sm btn-outline-warning", { disabled: i === category.questions.length - 1 })}>↓
            </button>
            <button onClick={() => onUpdate(d => {
              d.questions.splice(i, 1);
            })} className="btn btn-sm btn-outline-danger">✕
            </button>
          </div>
        </div>
      ))}

      <form onSubmit={e => {
        e.preventDefault();
        const f = new FormData(e.currentTarget);
        const question = (f.get("question") as string).trim();
        const answer = (f.get("answer") as string).trim();
        const worth = Number(f.get("worth")) as WorthType;
        if (!question || !answer || !worth) return;
        onUpdate(d => {
          d.questions.push({ question, answer, worth });
        });
        e.currentTarget.reset();
      }} className="d-flex flex-column gap-2">
        <input className="form-control form-control-sm" name="question" placeholder="Question" required />
        <input className="form-control form-control-sm" name="answer" placeholder="Answer" required />
        <select className="form-select form-select-sm" name="worth" required defaultValue="">
          <option value="" disabled>Worth</option>
          {Worths.filter(notExistsInCategoryFilter(category)).map(w => <option key={w} value={w}>{w}</option>)}
        </select>
        <button type="submit" className="btn btn-sm btn-outline-primary mt-1">Add Question</button>
      </form>
    </div>
  </>;
}

function EditAddCategoryForm({ existing, onAdd }: { existing: string[], onAdd: (name: string) => void }) {
  return (
    <form className="d-flex flex-column gap-3 p-1" onSubmit={e => {
      e.preventDefault();
      const form = e.currentTarget;
      const name = (new FormData(form).get("name") as string).trim();
      if (!name || existing.includes(name)) return;
      form.reset();
      onAdd(name);
    }}>
      <input className="form-control" name="name" placeholder="New category" autoComplete="off" />
      <div className="d-flex justify-content-center">
        <button type="submit" className="btn btn-outline-success" style={{ maxWidth: 80 }}>Add</button>
      </div>
    </form>
  );
}

export function Launch() {
  const { id } = useParams();
  const [players, setPlayers] = useState<string[]>([]);
  const getQuizQuery = useQuery({
    queryKey: ["quiz", id],
    queryFn: () => apiService.getQuiz(id!),
  });

  const [serverError, setServerError] = useState<string | null>(null);
  const clearServerError = setServerError.bind(null, null);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const launchMutation = useMutation({
    mutationFn: async (game: NewGameType) => {
      setServerError(null);
      return apiService.createGame(game);
    },
    onSuccess: (game) => {
      queryClient.invalidateQueries({ queryKey: ["games"] }).then();
      navigate(`/play/${game.id}`);
    },
    onError: (err: any) => {
      setServerError(err.message ?? "Unknown error");
    },
  });

  if (getQuizQuery.isLoading)
    return <p>Loading...</p>;

  const quiz = getQuizQuery.data;
  if (quiz === undefined)
    return <p>Failed to load quiz {id}</p>;

  return <>
    <h1>Launch</h1>
    <p>Start a game with the quiz "{quiz.name}" ({quiz.id})</p>
    <p>Players:</p>
    {players.length === 0 ? <p>No players yet</p> : players.map(p => {
      return <p key={p}>Player: {p}</p>;
    })}
    <form
      onSubmit={e => {
        e.preventDefault();
        setPlayers(players.concat(new FormData(e.currentTarget).get("player") as string));
        e.currentTarget.reset();
      }}
    >
      <input type="text" autoComplete="off" name="player" />
      <button type="submit" className="btn">Add</button>
    </form>
    <div style={{ maxWidth: 400 }}>
      {serverError && (
        <div className="alert alert-danger alert-dismissible" role="alert">
          {serverError}
          <button type="button" className="btn-close" aria-label="Close" onClick={clearServerError}></button>
        </div>
      )}
    </div>
    <button
      type="button"
      className={classNames(
        "btn btn-success",
        { disabled: players.length < 3 || launchMutation.isPending },
      )}
      onClick={() => launchMutation.mutate({
        name: `${quiz.name} - ${new Date().toISOString()}`,
        players,
        current: players[0],
        quizId: quiz.id,
        answers: [],
      })}
    >
      {!launchMutation.isPending ? "Launch" : "Launching..."}
    </button>
  </>;
}

export function PlayList() {
  return <>
    <h1>PlayList</h1>
    <p>PlayList</p>
  </>;
}

export function Play() {
  const { id } = useParams()

  const getGameQuery = useQuery({
    queryKey: ["game", id],
    queryFn: async () => {
      const game = await apiService.getGame(id!);
      const quiz = await apiService.getQuiz(game.quizId);
      return { game, quiz };
    },
  });

  if (getGameQuery.isFetching)
    return <p>Loading...</p>

  return <>
    <h1>Play</h1>
    <p>Play game {getGameQuery.data?.game.name}</p>
    <div className="d-flex gap-5 mb-3">
      {getGameQuery.data?.game.players.map(p => (
        <span
          key={p}
          className={classNames({"fw-bold": p === getGameQuery.data?.game.current})}
        >
          {p === getGameQuery.data?.game.current ? "Current" : "Player"}: {p}
        </span>
      ))}
    </div>

    <div className="container">
      <div className="row">
        {getGameQuery.data?.quiz.categories.filter(Boolean).map(category => (
          <div className="col" key={category.ordinal}>
            <p>category: {category.name}</p>
          </div>
        ))}
      </div>
    </div>

    <pre>{JSON.stringify(getGameQuery.data, null, 2)}</pre>
  </>;
}
