import axios from "axios";
import { API_BASE_URL } from "./index";

// exemplo de endpoints REST avaliativos
export const fetchNoticias = () => axios.get(`${API_BASE_URL}/noticias`);
export const fetchNoticia = (id) => axios.get(`${API_BASE_URL}/public/artigos/${id}`);

export const getAvaliacoesResumo = (id) =>
    axios.get(`${API_BASE_URL}/artigos/${id}/avaliacoes`).then(res => res.data);

export const avaliarArtigo = (id, tipo) =>
    axios.post(`${API_BASE_URL}/artigos/${id}/avaliar?avaliacao=${tipo}`);
