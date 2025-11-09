export type TaskStatus = "to-do" | "in-progress" | "blocked" | "done";

export type UserRole = "admin" | "project-manager" | "developer";

export interface IProject {
  _id: string;
  name: string;
  description: string;
  createdAt: Date;
}

export interface IUser {
  _id: string;
  name: string;
  email: string;
  password: string;
  role: UserRole;
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Interface for the Task document.
 */
export interface ITask {
  _id: string;
  title: string;
  description: string;
  status: TaskStatus;
  blockReason?: string;
  projectId: string;
  assignedTo: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface IResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}
