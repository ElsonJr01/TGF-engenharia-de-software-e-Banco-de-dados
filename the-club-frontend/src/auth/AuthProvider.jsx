import React, { createContext, useContext, useState, useEffect } from "react";
import PropTypes from "prop-types";
import api from "../api/api";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem("token"));

    useEffect(() => {
        if (token) {
            api.defaults.headers.Authorization = `Bearer ${token}`;
            setUser({ nome: "Admin", tipo: "ADMIN" }); // Placeholder - pode buscar do backend
        }
    }, [token]);

    const login = async (email, senha) => {
        try {
            const response = await api.post("/auth/login", { email, senha });
            const { token } = response.data;
            localStorage.setItem("token", token);
            setToken(token);
            setUser({ nome: email });
            return true;
        } catch {
            return false;
        }
    };

    const logout = () => {
        localStorage.removeItem("token");
        setUser(null);
        setToken(null);
    };

    return (
        <AuthContext.Provider value={{ token, user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

AuthProvider.propTypes = {
    children: PropTypes.node
};

export const useAuth = () => useContext(AuthContext);
