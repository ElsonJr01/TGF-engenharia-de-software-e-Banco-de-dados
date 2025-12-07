import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "../pages/Home";
import Editais from "../pages/Editais";
import NoticiaDetalhes from "../pages/NoticiaDetalhes";
import Login from "../pages/Login";
import Cadastro from "../pages/Cadastro";
import Dashboard from "../pages/admin/Dashboard";
import NovaNoticia from "../pages/admin/NovaNoticia";
import EditarNoticia from "../pages/admin/EditarNoticia";
import Categorias from "../pages/Categorias"; // público
import AdminCategorias from "../pages/admin/Categorias"; // admin

function AppRouter() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/editais" element={<Editais />} />
                <Route path="/noticia/:id" element={<NoticiaDetalhes />} />
                <Route path="/admin/login" element={<Login />} />
                <Route path="/admin/dashboard" element={<Dashboard />} />
                <Route path="/admin/artigos/novo" element={<NovaNoticia />} />
                <Route path="/admin/artigos/editar/:id" element={<EditarNoticia />} />
                <Route path="/cadastro" element={<Cadastro />} />
                <Route path="/categorias" element={<Categorias />} />
                <Route path="/admin/categorias" element={<AdminCategorias />} />
            </Routes>
        </Router>
    );
}

export default AppRouter;
