import { Request, Response } from "express";
import { IResponse, IUser, UserRole } from "../types/types";
const UserModel = require("../models/UserModel");
const TokenModel = require("../models/TokenModel");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");

interface AuthLoginReqBody {
  email: string;
  password: string;
}

interface AuthRegisterReqBody {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

export const authLogin = async (
  req: Request<{}, {}, AuthLoginReqBody>,
  res: Response<IResponse<{ token: string; refreshToken: string }>>
) => {
  try {
    const { email, password } = req.body;
    const User: IUser | null = await UserModel.findOne({ email });
    if (!User) {
      return res
        .status(401)
        .json({ message: "Invalid email or password", success: false });
    }
    const isPasswordValid = await bcrypt.compare(password, User.password);
    if (!isPasswordValid) {
      return res
        .status(401)
        .json({ message: "Invalid email or password", success: false });
    }
    const token = generateAccessToken(User._id, User.email, User.role);
    const refreshToken = await generateRefreshToken(User._id);
    return res.status(200).json({
      message: "Login successful",
      success: true,
      data: {
        token,
        refreshToken,
      },
    });
  } catch (error) {
    console.error("Error logging in:", error);
    res.status(500).json({
      message: "Failed to login",
      success: false,
      error: (error as Error).message,
    });
  }
};

export const authRegister = async (
  req: Request<{}, {}, AuthRegisterReqBody>,
  res: Response<
    IResponse<{
      token: string;
      refreshToken: string;
      email: string;
      role: UserRole;
    }>
  >
) => {
  try {
    const { name, email, password, role } = req.body;
    const existingUser = await UserModel.findOne({ email });
    if (existingUser) {
      return res
        .status(400)
        .json({ message: "User already exists", success: false });
    }
    const hashedPassword = await bcrypt.hash(password, 10);
    const User: IUser = await UserModel.create({
      name,
      email,
      password: hashedPassword,
      role,
    });
    const token = generateAccessToken(User._id, User.email, User.role);
    const refreshToken = await generateRefreshToken(User._id);
    return res.status(200).json({
      message: "User registered successfully",
      success: true,
      data: {
        token,
        refreshToken,
        email: User.email,
        role: User.role,
      },
    });
  } catch (error) {
    console.error("Error registering user:", error);
    res.status(500).json({
      message: "Failed to register user",
      success: false,
      error: (error as Error).message,
    });
  }
};

export const authRefreshToken = async (
  req: Request<{}, {}, { refreshToken: string }>,
  res: Response<IResponse<{ token: string; refreshToken: string }>>
) => {
  try {
    const { refreshToken } = req.body;
    const decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET);
    if (!decoded) {
      return res
        .status(401)
        .json({ message: "Invalid refresh token", success: false });
    }
    const token = generateAccessToken(decoded.id, decoded.email, decoded.role);
    const newRefreshToken = await generateRefreshToken(decoded.id);
    return res.status(200).json({
      message: "Token refreshed successfully",
      success: true,
      data: {
        token,
        refreshToken: newRefreshToken,
      },
    });
  } catch (error) {
    console.error("Error refreshing token:", error);
    res.status(500).json({
      message: "Failed to refresh token",
      success: false,
      error: (error as Error).message,
    });
  }
};

export const authLogout = async (
  req: Request<{}, {}, { userId: string }>,
  res: Response<IResponse<void>>
) => {
  try {
    const userId = req.userId;
    await TokenModel.findOneAndDelete({ userId });
    return res
      .status(200)
      .json({ message: "Logout successful", success: true });
  } catch (error) {
    console.error("Error logging out:", error);
    res.status(500).json({
      message: "Failed to logout",
      success: false,
      error: (error as Error).message,
    });
  }
};

const generateAccessToken = (userId: string, email: string, role: UserRole) => {
  return jwt.sign({ id: userId, email, role }, process.env.JWT_SECRET, {
    expiresIn: "15m",
  });
};

const generateRefreshToken = async (userId: string) => {
  const refreshToken = jwt.sign(
    { id: userId },
    process.env.JWT_REFRESH_SECRET,
    { expiresIn: "3d" }
  );
  await TokenModel.create({ userId, refreshToken });
  return refreshToken;
};
