import omit from "lodash/omit";
import { type GameType, type NewGameType, type NewQuizType, type QuizType } from "./models.ts";

export class ApiService {
  private quizzes: QuizType[] = [];
  private games: GameType[] = [];

  async write() {
    localStorage.setItem("quizzes", JSON.stringify(this.quizzes));
    localStorage.setItem("games", JSON.stringify(this.games));
  }

  async read() {
    let item = localStorage.getItem("quizzes");
    if (item != null) {
      this.quizzes = JSON.parse(item);
    }
    item = localStorage.getItem("games");
    if (item != null) {
      this.games = JSON.parse(item);
    }
  }

  async createQuiz(newQuiz: NewQuizType): Promise<QuizType> {
    await new Promise((r) => setTimeout(r, 1000));
    await this.read();

    if (this.quizzes.filter(q => q.name === newQuiz.name).length > 0)
      throw new Error("Quiz name already exists: " + newQuiz.name);

    let now = new Date();
    const quiz: QuizType = {
      ...newQuiz,
      id: crypto.randomUUID(),
      created: now.toISOString(),
      updated: now.toISOString(),
      categories: [],
    };

    this.quizzes.push(quiz);
    await this.write();
    return quiz;
  }

  async getQuiz(id: string): Promise<QuizType> {
    // await new Promise((r) => setTimeout(r, 1000));
    await this.read();
    let result = this.quizzes.find(q => q.id === id);
    if (!result)
      throw new Error("could not find " + id);
    return result;
  }

  async listQuizzes(): Promise<QuizType[]> {
    await new Promise((r) => setTimeout(r, 1000));
    await this.read();
    return [...this.quizzes];
  }

  async updateQuiz(id: string, patch: Partial<QuizType>): Promise<QuizType> {
    await new Promise((r) => setTimeout(r, 1000));
    await this.read();
    const quiz = await this.getQuiz(id);
    if (!quiz)
      throw new Error("could not find " + id);

    patch = omit(patch, ["id", "created", "updated"]);
    Object.assign(quiz, patch, { updated: new Date().toISOString() });
    await this.write();
    return quiz;
  }

  async deleteQuiz(id: string): Promise<void> {
    await new Promise((r) => setTimeout(r, 1000));
    await this.read();
    const index = this.quizzes.findIndex(q => q.id === id);
    if (index === -1)
      throw new Error("could not find " + id);
    this.quizzes.splice(index, 1);
    await this.write();
    return;
  }

  async createGame(newGame: NewGameType): Promise<GameType> {
    await new Promise((r) => setTimeout(r, 1000));
    await this.read();

    if (this.games.filter(q => q.name === newGame.name).length > 0)
      throw new Error("Game name already exists: " + newGame.name);

    let now = new Date();
    const quiz: GameType = {
      ...newGame,
      id: crypto.randomUUID(),
      created: now.toISOString(),
      updated: now.toISOString(),
    };

    this.games.push(quiz);
    await this.write();
    return quiz;
  }

  async getGame(id: string): Promise<GameType> {
    await this.read();
    let result = this.games.find(g => g.id === id);
    if (!result)
      throw new Error("could not find " + id);
    return result;
  }
}

export const apiService = new ApiService();
