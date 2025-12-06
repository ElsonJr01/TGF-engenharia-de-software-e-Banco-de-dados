import React, { useEffect, useState } from "react";
import axios from "axios";

function Categorias() {
    const [categorias, setCategorias] = useState([]);

    useEffect(() => {
        axios
            .get("http://localhost:8081/api/public/categorias")
            .then((response) => setCategorias(response.data))
            .catch((error) => console.error("Erro ao carregar categorias:", error));
    }, []);

    return (
        <div className="container mt-4">
            <h2>Categorias de Artigos</h2>
            <table className="table table-bordered mt-3">
                <thead className="table-light">
                <tr>
                    <th>Nome</th>
                    <th>Descrição</th>
                    <th>Cor</th>
                </tr>
                </thead>
                <tbody>
                {categorias.length > 0 ? (
                    categorias.map((categoria) => (
                        <tr key={categoria.id}>
                            <td>{categoria.nome}</td>
                            <td>{categoria.descricao}</td>
                            <td>
                  <span
                      style={{
                          backgroundColor: categoria.cor,
                          padding: "5px 15px",
                          borderRadius: "5px",
                          color: "white",
                      }}
                  >
                    {categoria.cor}
                  </span>
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="3" className="text-center text-muted">
                            Nenhuma categoria cadastrada.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default Categorias;
