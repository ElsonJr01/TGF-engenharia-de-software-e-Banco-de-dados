import React, { useEffect, useState } from "react";
import axios from "axios";

function Eventos() {
    const [eventos, setEventos] = useState([]);

    useEffect(() => {
        axios
            .get("http://localhost:8081/api/public/eventos")
            .then((response) => setEventos(response.data.content || []))
            .catch((error) => console.error("Erro ao carregar eventos:", error));
    }, []);

    return (
        <div className="container mt-4">
            <h2>Eventos Universitários</h2>
            <div className="row mt-3">
                {eventos.length > 0 ? (
                    eventos.map((evento) => (
                        <div className="col-md-6 col-lg-4 mb-3" key={evento.id}>
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">{evento.titulo}</h5>
                                    <p className="card-text text-muted">{evento.descricao}</p>
                                    <p>
                                        <strong>Data:</strong>{" "}
                                        {new Date(evento.dataEvento).toLocaleDateString("pt-BR")}
                                    </p>
                                    <p>
                                        <strong>Local:</strong> {evento.localEvento}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))
                ) : (
                    <p className="text-muted text-center">Nenhum evento disponível.</p>
                )}
            </div>
        </div>
    );
}

export default Eventos;
