import * as axios from "axios";
import {useAuthStore} from "../stores/authStore.ts";



export const apiClient = axios.create();

apiClient.interceptors.request.use(config => {
    const accessToken = useAuthStore.getState().tokens?.accessToken;

    if(accessToken) {
        config.headers.set('Authorization', `Bearer ${accessToken}`)
    }

    return config
})