import { configureStore } from "@reduxjs/toolkit";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { Page, Pageable, Task } from "./models.tsx";

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
    createTask: builder.mutation<Task, Task>({
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
    })
  }),
});

export const store = configureStore({
  reducer: {
    [tasksApi.reducerPath]: tasksApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(tasksApi.middleware),
});

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch
