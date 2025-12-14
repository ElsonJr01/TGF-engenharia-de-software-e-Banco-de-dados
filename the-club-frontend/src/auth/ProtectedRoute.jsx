import React from "react";
import PropTypes from "prop-types";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthProvider";

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { token, user } = useAuth();

    // Se não estiver logado, redireciona para login
    if (!token) return <Navigate to="/login" replace />;

    // Se houver restrição de roles (ADMIN, EDITOR, etc)
    if (allowedRoles && !allowedRoles.includes(user?.tipo)) {
        return <Navigate to="/" replace />;
    }

    return children;
};

ProtectedRoute.propTypes = {
    children: PropTypes.node,
    allowedRoles: PropTypes.arrayOf(PropTypes.string)
};

export default ProtectedRoute;
