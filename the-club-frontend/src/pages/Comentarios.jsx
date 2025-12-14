import React, { useEffect, useState } from "react";
import axios from "axios";

function Comentarios() {
    const [comentarios, setComentarios] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        axios
            .get("http://localhost:8081/api/admin/comentarios")
            .then((response) => {
                setComentarios(response.data);
                setLoading(false);
            })
            .catch((error) => {
                console.error("Erro ao carregar comentários:", error);
                setLoading(false);
            });
    }, []);

    const aprovarComentario = (id) => {
        axios
            .put(`http://localhost:8081/api/admin/comentarios/${id}/aprovar`)
            .then(() => {
                setComentarios(
                    comentarios.map((c) =>
                        c.id === id ? { ...c, aprovado: true } : c
                    )
                );
            })
            .catch((error) => console.error("Erro ao aprovar:", error));
    };

    const reprovarComentario = (id) => {
        axios
            .delete(`http://localhost:8081/api/admin/comentarios/${id}`)
            .then(() => {
                setComentarios(comentarios.filter((c) => c.id !== id));
            })
            .catch((error) => console.error("Erro ao deletar:", error));
    };

    if (loading) return <p className="text-center mt-4">Carregando comentários...</p>;

    return (
        <div className="container mt-4">
            <h2>Gerenciamento de Comentários</h2>
            <p className="text-muted">
                {comentarios.filter((c) => !c.aprovado).length} comentário(s) pendente(s)
            </p>

            <table className="table table-striped mt-3">
                <thead>
                <tr>
                    <th>Artigo</th>
                    <th>Usuário</th>
                    <th>Comentário</th>
                    <th>Data</th>
                    <th>Status</th>
                    <th>Ações</th>
                </tr>
                </thead>
                <tbody>
                {comentarios.length > 0 ? (
                    comentarios.map((comentario) => (
                        <tr key={comentario.id}>
                            <td>{comentario.artigoTitulo || "N/A"}</td>
                            <td>{comentario.usuarioNome || "Anônimo"}</td>
                            <td>
                                {comentario.comentario.length > 50
                                    ? comentario.comentario.substring(0, 50) + "..."
                                    : comentario.comentario}
                            </td>
                            <td>
                                {new Date(comentario.dataComentario).toLocaleDateString(
                                    "pt-BR"
                                )}
                            </td>
                            <td>
                                {comentario.aprovado ? (
                                    <span className="badge bg-success">Aprovado</span>
                                ) : (
                                    <span className="badge bg-warning">Pendente</span>
                                )}
                            </td>
                            <td>
                                {!comentario.aprovado ? (
                                    <>
                                        <button
                                            className="btn btn-sm btn-success me-2"
                                            onClick={() => aprovarComentario(comentario.id)}
                                        >
                                            Aprovar
                                        </button>
                                        <button
                                            className="btn btn-sm btn-danger"
                                            onClick={() => reprovarComentario(comentario.id)}
                                        >
                                            Deletar
                                        </button>
                                    </>
                                ) : (
                                    <button
                                        className="btn btn-sm btn-danger"
                                        onClick={() => reprovarComentario(comentario.id)}
                                    >
                                        Deletar
                                    </button>
                                )}
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="6" className="text-center text-muted">
                            Nenhum comentário encontrado.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default Comentarios;
