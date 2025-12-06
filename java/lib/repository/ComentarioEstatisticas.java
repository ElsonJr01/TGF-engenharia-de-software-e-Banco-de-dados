package lib.repository;

/**
 * DTO de projeção para estatísticas de comentários.
 */
public class ComentarioEstatisticas {

    private final long total;
    private final long aprovados;
    private final long pendentes;

    public ComentarioEstatisticas(long total, long aprovados, long pendentes) {
        this.total = total;
        this.aprovados = aprovados;
        this.pendentes = pendentes;
    }

    public long getTotal() {
        return total;
    }

    public long getAprovados() {
        return aprovados;
    }

    public long getPendentes() {
        return pendentes;
    }

    @Override
    public String toString() {
        return "ComentarioEstatisticas{" +
                "total=" + total +
                ", aprovados=" + aprovados +
                ", pendentes=" + pendentes +
                '}';
    }
}
