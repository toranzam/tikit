import {create} from "zustand/react";
import {createJSONStorage, persist} from "zustand/middleware";

interface Tokens {
    accessToken: string
    refreshToken: string
}

interface AuthState {
    tokens: Tokens | null;
    setTokens: (tokens: Tokens) => void;
    clearTokens: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        set => ({
            tokens: null,
            setTokens: tokens => set({ tokens }),
            clearTokens: () => set({ tokens: null }),
        }),
        {
            name: 'tikit-auth',
            storage: createJSONStorage(() => sessionStorage),
            partialize: state => ({
                tokens: state.tokens,
            }),
        },
    ),
);