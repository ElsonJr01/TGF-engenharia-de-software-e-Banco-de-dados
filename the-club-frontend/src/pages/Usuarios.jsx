import React, { useEffect, useState } from "react";
import axios from "axios";

function Usuarios() {
    const [usuarios, setUsuarios] = useState([]);

    useEffect(() => {
        axios
            .get("http://localhost:8081/api/admin/usuarios")
            .then((response) => setUsuarios(response.data))
            .catch((error) => console.error("Erro ao carregar usuários:", error));
    }, []);

    return (
        <div className="container mt-4">
            <h2>Gerenciamento de Usuários</h2>
            <table className="table table-hover mt-3">
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>Email</th>
                    <th>Tipo</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                {usuarios.length > 0 ? (
                    usuarios.map((usuario) => (
                        <tr key={usuario.id}>
                            <td>{usuario.nome}</td>
                            <td>{usuario.email}</td>
                            <td>{usuario.tipo}</td>
                            <td>{usuario.ativo ? "Ativo" : "Inativo"}</td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="4" className="text-center text-muted">
                            Nenhum usuário cadastrado.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default Usuarios;
