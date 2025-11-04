const TaskModel = require("../models/TaskModel");
const ProjectModel = require("../models/ProjectModel");
import { Request, Response } from "express";
import { IProject, ITask, TaskStatus } from "../types/types";

interface CreateTaskReqBody {
  title: string;
  description: string;
  status: TaskStatus;
  blockReason?: string;
  projectId: string;
  assignedTo?: string;
}

interface GetTasksReqQuery {
  projectId?: string;
}

interface UpdateTaskReqBody {
  title?: string;
  description?: string;
  status?: TaskStatus;
  blockReason?: string;
  assignedTo?: string;
}

export const createTask = async (
  req: Request<{}, {}, CreateTaskReqBody>,
  res: Response<{ success: boolean; error?: string; message: string; data?: any }>
) => {
  try {
    const { title, description, status, blockReason, projectId, assignedTo } = req.body;

    const Project: IProject | null = await ProjectModel.findById(projectId);
    if (!Project) {
      return res
        .status(404)
        .json({ success: false, message: "Project not found" });
    }

    const createdTask = await TaskModel.create({
      title,
      description,
      status,
      blockReason,
      projectId,
      assignedTo,
    });

    res.status(200).json({
      success: true,
      message: "Task created successfully",
      data: {
        id: createdTask._id?.toString() || createdTask.id?.toString() || "",
        title: createdTask.title,
        description: createdTask.description,
        status: createdTask.status,
        blockReason: createdTask.blockReason ?? "",
        projectId: createdTask.projectId?.toString() || "",
        assignedTo: createdTask.assignedTo?.toString() || null,
        createdAt: createdTask.createdAt,
      },
    });
  } catch (error) {
    console.error("Error creating task:", error);
    res.status(500).json({
      success: false,
      message: "Failed to create task",
      error: (error as Error).message,
    });
  }
};

export const getProjectTasks = async (
  req: Request<{}, {}, {}, GetTasksReqQuery>,
  res: Response<{
    data?: ITask[];
    success: boolean;
    error?: string;
    message?: string;
  }>
) => {
  const { projectId } = req.query;

  if (!projectId) {
    return res
      .status(400)
      .json({ success: false, message: "Project ID is required" });
  }

  try {
    const tasks: ITask[] = await TaskModel.find({ projectId });
    res.status(200).json({
      data: tasks.map((task) => {
        return {
          id: task._id?.toString() || task.id?.toString() || "",
          title: task.title,
          description: task.description,
          status: task.status,
          blockReason: task.blockReason ?? "",
          projectId: task.projectId?.toString() || "",
          assignedTo: task.assignedTo?.toString() || null,
          createdAt: task.createdAt,
        };
      }),
      success: true,
    });
  } catch (error) {
    console.error("Error fetching tasks:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch tasks",
      error: (error as Error).message,
    });
  }
};

export const updateTask = async (
  req: Request<{ id: string }, {}, UpdateTaskReqBody>,
  res: Response<{ success: boolean; error?: string; message?: string; data?: any }>
) => {
  const { id } = req.params;
  const updatePayload: UpdateTaskReqBody = req.body;

  // Validate: blockReason is required when status is "blocked"
  if (updatePayload.status === "blocked" && (!updatePayload.blockReason || updatePayload.blockReason.trim() === "")) {
    return res.status(400).json({
      success: false,
      message: "Block reason is required when status is 'blocked'",
    });
  }

  try {
    const task: ITask | null = await TaskModel.findByIdAndUpdate(
      id,
      updatePayload,
      { new: true }
    );

    if (!task) {
      return res
        .status(404)
        .json({ success: false, message: "Task not found" });
    }

    res.status(200).json({
      success: true,
      message: "Task updated successfully",
      data: {
        id: task._id?.toString() || task.id?.toString() || "",
        title: task.title,
        description: task.description,
        status: task.status,
        blockReason: task.blockReason ?? "",
        projectId: task.projectId?.toString() || "",
        assignedTo: task.assignedTo?.toString() || null,
        createdAt: task.createdAt,
        updatedAt: task.updatedAt,
      },
    });
  } catch (error) {
    console.error("Error updating task:", error);
    res.status(500).json({
      success: false,
      message: "Failed to update task",
      error: (error as Error).message,
    });
  }
};

export const deleteTask = async (
  req: Request<{ id: string }>,
  res: Response<{ success: boolean; error?: string; message?: string }>
) => {
  const { id } = req.params;
  try {
    const task: ITask | null = await TaskModel.findByIdAndDelete(id);

    if (!task) {
      return res
        .status(404)
        .json({ success: false, message: "Task not found" });
    }

    res
      .status(200)
      .json({ success: true, message: "Task deleted successfully" });
  } catch (error) {
    console.error("Error deleting task:", error);
    res.status(500).json({
      success: false,
      message: "Failed to delete task",
      error: (error as Error).message,
    });
  }
};
