import { configureStore, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { useDispatch, useSelector } from "react-redux";
import { NewTask, Page, Pageable, Task } from "./models.tsx";

const tasksApiTag = "Task" as const;
export const tasksApi = createApi({
  reducerPath: "tasksApi",
  baseQuery: fetchBaseQuery({ baseUrl: "/api/tasks" }),
  tagTypes: [tasksApiTag],
  endpoints: (builder) => ({
    getTasks: builder.query<Page<Task>, Pageable>({
      query: (pageable: Pageable) => {
        let path = "?";
        if (pageable.page) path += `page=${pageable.page}&`;
        if (pageable.size) path += `size=${pageable.size}&`;
        return path;
      },
      providesTags: (result) => {
        return ((result?.content || []) as Array<{ id: string }>)
          .map(({ id }) => ({ type: tasksApiTag, id }))
          .concat([{ type: tasksApiTag, id: "LIST" }]);
      },
    }),
    /**
     * create Task from NewTask
     */
    createTask: builder.mutation<Task, NewTask>({
      query(body: Task) {
        return { url: "", method: "POST", body };
      },
      invalidatesTags: [{ type: tasksApiTag, id: "LIST" }],
    }),
    getTaskById: builder.query<Task, string>({
      query: (id) => `/${id}`,
    }),
    updateTaskById: builder.mutation<Task, Task>({
      query(body: Task) {
        return { url: `/${body.id}`, method: "PUT", body };
      },
      invalidatesTags: result => result?.id ? [{ type: tasksApiTag, id: result.id }] : [],
    }),
    deleteTaskById: builder.mutation<Task, Task>({
      query(body: Task) {
        return { url: `/${body.id}`, method: "DELETE" };
      },
      invalidatesTags: result => result?.id ? [{ type: tasksApiTag, id: result.id }] : [],
    }),
  }),
});

export interface TaskUiTask {
  editing: boolean;
  newData: Task;
}

export const tasksUi = createSlice({
  name: "tasksUi",
  initialState: {
    pageable: {
      page: 0,
      size: 3,
    },
    tasks: ({} as Record<string, TaskUiTask>),
  },
  reducers: {
    startEditingPost(state, action: PayloadAction<Task>) {
      state.tasks[action.payload.id] = state.tasks[action.payload.id] || {};
      state.tasks[action.payload.id].editing = true;
      state.tasks[action.payload.id].newData = state.tasks[action.payload.id].newData || action.payload;
    },
    stopEditingPost(state, action: PayloadAction<string>) {
      state.tasks[action.payload] = state.tasks[action.payload] || {};
      state.tasks[action.payload].editing = false;
    },
    edit(state, action: PayloadAction<Task>) {
      state.tasks[action.payload.id] = state.tasks[action.payload.id] || {};
      state.tasks[action.payload.id].newData = action.payload;
    },
    goToPage(state, action: PayloadAction<number>) {
      state.pageable.page = action.payload;
    },
    changePageSize(state, action: PayloadAction<number>) {
      state.pageable.size = action.payload;
    },
  },
});

export const store = configureStore({
  reducer: {
    [tasksApi.reducerPath]: tasksApi.reducer,
    [tasksUi.reducerPath]: tasksUi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(tasksApi.middleware),
});

// https://react-redux.js.org/tutorials/typescript-quick-start
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch

// https://react-redux.js.org/using-react-redux/usage-with-typescript
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
// export const useAppStore = useStore.withTypes<RootState>()
