import { Request, Response } from "express";
import { IProject, ITask, IUser, UserRole } from "../types/types";
const ProjectModel = require("../models/ProjectModel");
const UserModel = require("../models/UserModel");
const TaskModel = require("../models/TaskModel");

interface CreateProjectReqBody {
  name: string;
  description: string;
}

interface UpdateProjectReqBody {
  name?: string;
  description?: string;
}

export const createProject = async (
  req: Request<{}, {}, CreateProjectReqBody>,
  res: Response<{ message: string; success: boolean; error?: string }>
) => {
  try {
    const { name, description } = req.body;
    const assignedTo = req.userId;

    if (!assignedTo) {
      return res.status(401).json({ success: false, message: "Unauthorized" });
    }

    const User: IUser | null = await UserModel.findById(assignedTo);
    if (!User) {
      return res
        .status(404)
        .json({ success: false, message: "User not found" });
    }

    if (!isUserManager(User._id, "project-manager")) {
      return res.status(403).json({
        success: false,
        message: "User is not authorized to create a project",
      });
    }

    await ProjectModel.create({
      name,
      description,
      assignedTo,
    });
    res.status(200).json({
      message: "Project created successfully",
      success: true,
    });
  } catch (error) {
    console.error("Error creating project:", error);
    res.status(500).json({
      message: "Failed to create project",
      success: false,
      error: (error as Error).message,
    });
  }
};

export const getProjects = async (
  req: Request<{}, {}, {}>,
  res: Response<{
    data?: IProject[];
    success: boolean;
    error?: string;
    message?: string;
  }>
) => {
  try {
    const assignedTo = req.userId;

    if (!assignedTo) {
      return res.status(401).json({ success: false, message: "Unauthorized" });
    }
    const isManager = await isUserManager(assignedTo, "project-manager");
    if (isManager) {
      const projects: IProject[] = await getManagerProjects(assignedTo);
      if (!projects || projects.length === 0) {
        return res
          .status(200)
          .json({ success: true, data: [], message: "No projects found" });
      }
      return res.status(200).json({
        success: true,
        data: projects.map((project) => ({
          _id: project._id?.toString() || "",
          name: project.name,
          description: project.description,
          createdAt: project.createdAt,
        })),
      });
    } else {
      const projects: IProject[] = await getDeveloperProjects(assignedTo);
      if (!projects || projects.length === 0) {
        return res
          .status(200)
          .json({ success: true, data: [], message: "No projects found" });
      }
      return res.status(200).json({
        success: true,
        data: projects.map((project) => ({
          _id: project._id?.toString() || "",
          name: project.name,
          description: project.description,
          createdAt: project.createdAt,
        })),
      });
    }
  } catch (error) {
    console.error("Error fetching projects:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch projects",
      error: (error as Error).message,
    });
  }
};

export const getProject = async (
  req: Request<{ id: string }>,
  res: Response<{
    data?: IProject;
    success: boolean;
    error?: string;
    message?: string;
  }>
) => {
  try {
    const { id } = req.params;
    const project: IProject | null = await ProjectModel.findById(id);
    if (!project) {
      return res
        .status(404)
        .json({ success: false, message: "Project not found" });
    }
    res.status(200).json({
      data: {
        _id: project._id?.toString() || "",
        name: project.name,
        description: project.description,
        createdAt: project.createdAt,
      },
      success: true,
    });
  } catch (error) {
    console.error("Error fetching project:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch project",
      error: (error as Error).message,
    });
  }
};

export const updateProject = async (
  req: Request<{ id: string }, {}, UpdateProjectReqBody>,
  res: Response<{ success: boolean; error?: string; message?: string }>
) => {
  try {
    const { id } = req.params;
    const { name, description } = req.body;

    const updatePayload: UpdateProjectReqBody = {};
    if (name !== undefined) updatePayload.name = name;
    if (description !== undefined) updatePayload.description = description;

    const project: IProject | null = await ProjectModel.findByIdAndUpdate(
      id,
      updatePayload,
      { new: true }
    );

    if (!project) {
      return res
        .status(404)
        .json({ success: false, message: "Project not found" });
    }
    res
      .status(200)
      .json({ success: true, message: "Project updated successfully" });
  } catch (error) {
    console.error("Error updating project:", error);
    res.status(500).json({
      success: false,
      message: "Failed to update project",
      error: (error as Error).message,
    });
  }
};

export const deleteProject = async (
  req: Request<{ id: string }>,
  res: Response<{ success: boolean; error?: string; message?: string }>
) => {
  try {
    const { id } = req.params;
    const project: IProject | null = await ProjectModel.findByIdAndDelete(id);
    if (!project) {
      return res
        .status(404)
        .json({ success: false, message: "Project not found" });
    }
    res
      .status(200)
      .json({ success: true, message: "Project deleted successfully" });
  } catch (error) {
    console.error("Error deleting project:", error);
    res.status(500).json({
      success: false,
      message: "Failed to delete project",
      error: (error as Error).message,
    });
  }
};

export const isUserManager = async (userId: string, role: UserRole) => {
  const User: IUser | null = await UserModel.findById(userId);
  if (!User) {
    return false;
  }
  return User.role === role;
};

const getManagerProjects = async (userId: string) => {
  const projects: IProject[] = await ProjectModel.find({ assignedTo: userId });
  return projects;
};

const getDeveloperProjects = async (userId: string) => {
  const tasks: ITask[] = await TaskModel.find({ assignedTo: userId });
  const projects: IProject[] = await ProjectModel.find({
    _id: { $in: tasks.map((task) => task.projectId) },
  });
  return projects;
};
