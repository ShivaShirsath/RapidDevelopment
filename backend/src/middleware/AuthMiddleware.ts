import { NextFunction } from "express";
import { Request, Response } from "express";
import { IUser } from "../types/types";
const jwt = require("jsonwebtoken");
const UserModel = require("../models/UserModel");

// Extend Express Request to include userId
declare global {
  namespace Express {
    interface Request {
      userId?: string;
    }
  }
}

export const authMiddleware = async (req: Request, res: Response, next: NextFunction) => {
  // Support both standard Authorization header and custom token header
  let token: string | undefined;
  
  // Check for Authorization: Bearer <token>
  const authHeader = req.headers.authorization;
  if (authHeader && authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7); // Remove "Bearer " prefix
  } else {
    // Fallback to custom token header (for backward compatibility)
    token = req.headers.token as string | undefined;
  }

  if (!token) {
    return res.status(401).json({ success: false, message: "Unauthorized" });
  }
  
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    if (!decoded) {
      return res.status(401).json({ success: false, message: "Unauthorized" });
    }
    const User: IUser | null = await UserModel.findOne({ email: decoded.email });
    if (!User) {
      return res.status(401).json({ success: false, message: "Unauthorized" });
    }
    // Attach userId to request object
    req.userId = decoded.id;
    next();
  } catch (error) {
    return res.status(401).json({ success: false, message: "Invalid token" });
  }
}
