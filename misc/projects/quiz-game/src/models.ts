import { array, enums, type Infer, number, object, omit, string } from "superstruct";

// Enum for question worth
export const Worths = [100, 200, 300, 400, 500];
const Worth = enums(Worths);

export type WorthType = Infer<typeof Worth>

// Question structure
const Question = object({
  question: string(),
  answer: string(),
  worth: Worth,
});

export type QuestionType = Infer<typeof Question>

// Category structure
const Category = object({
  name: string(),
  ordinal: number(),
  questions: array(Question),
});

export type CategoryType = Infer<typeof Category>

// Quiz structure
export const Quiz = object({
  id: string(),
  name: string(),
  created: string(), // ISO date string (or use instance(Date) if you want actual Date objects)
  updated: string(),
  categories: array(Category),
});

// Type inference
export type QuizType = Infer<typeof Quiz>

export const NewQuiz = omit(Quiz, ["id", "created", "updated"])

export type NewQuizType = Infer<typeof NewQuiz>

export const Answer = object({
  player: string(),
  category: string(),
  worth: Worth,
});

export type AnswerType = Infer<typeof Answer>

export const Game = object({
  id: string(),
  name: string(),
  created: string(),
  updated: string(),
  players: array(string()),
  current: string(),
  quizId: string(),
  answers: array(Answer),
});

export type GameType = Infer<typeof Game>

export const NewGame = omit(Game, ["id", "created", "updated"]);

export type NewGameType = Infer<typeof NewGame>
