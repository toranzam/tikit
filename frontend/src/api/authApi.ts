import {apiClient} from "./apiClient.ts";

interface LoginRequest {
    email: string
    password: string
}

interface LoginResponse {
    code: string
    message: string
    data: {
        accessToken: string
        refreshToken: string
    }
}

const API_PREFIX = '/api/v1/auth'

export async function login(request: LoginRequest) {
    const response = await apiClient.post<LoginResponse>(
        `${API_PREFIX}/login`,
        request
    )

    return response.data
}

interface SignupRequest {
    email: string
    password: string
    name: string
}

interface SignupResponse {
    code: string
    message: string
    data: null
}

export async function signup(request: SignupRequest) {
    const response = await apiClient.post<SignupResponse>(
        `${API_PREFIX}/signup`,
        request
    );

    return response.data;
}

