import React, { useEffect, useState } from "react";
import axios from "axios";

function Noticia() {
    const [artigos, setArtigos] = useState([]);

    useEffect(() => {
        axios
            .get("http://localhost:8081/api/public/artigos")
            .then((response) => setArtigos(response.data.content || []))
            .catch((error) => console.error("Erro ao carregar artigos:", error));
    }, []);

    return (
        <div className="container mt-4">
            <h2>Artigos Publicados</h2>
            <table className="table table-striped mt-3">
                <thead>
                <tr>
                    <th>Título</th>
                    <th>Categoria</th>
                    <th>Data</th>
                </tr>
                </thead>
                <tbody>
                {artigos.length > 0 ? (
                    artigos.map((artigo) => (
                        <tr key={artigo.id}>
                            <td>{artigo.titulo}</td>
                            <td>{artigo.categoriaNome}</td>
                            <td>
                                {new Date(artigo.dataPublicacao).toLocaleDateString("pt-BR")}
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="3" className="text-center text-muted">
                            Nenhum artigo encontrado.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default Noticia;
