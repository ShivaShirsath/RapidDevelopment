import { IResponse, IUser } from "../types/types";
import { Request, Response } from "express";
const UserModel = require("../models/UserModel");

export const getDevelopers = async (req: Request<{}, {}, {}>, res: Response<IResponse<IUser[]>>) => {
  try {
    const developers: IUser[] = await UserModel.find({ role: "developer" });
    return res.status(200).json({
      success: true,
      message: "Developers fetched successfully",
      data: developers.map((dev) => ({
        id: dev._id?.toString() || dev.id?.toString() || "",
        name: dev.name,
        email: dev.email,
        role: dev.role,
        createdAt: dev.createdAt,
      })),
    });
  } catch (error) {
    return res.status(500).json({ message: "Failed to get users", success: false, error: (error as Error).message });
  }
}