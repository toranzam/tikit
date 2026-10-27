import {create} from "zustand/react";

interface Tokens {
    accessToken: string
    refreshToken: string
}

interface AuthState {
    tokens: Tokens | null;
    setTokens: (tokens: Tokens) => void;
    clearTokens: () => void;
}

export const useAuthStore = create<AuthState>()(set => ({
    tokens: null,
    setTokens: (tokens => set({tokens})),
    clearTokens: () => set({tokens: null}),
}))