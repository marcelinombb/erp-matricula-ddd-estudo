package br.com.escola.matricula.dominio.servico;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class MatriculaRepositorioEmMemoria implements MatriculaRepositorio {

    private boolean existeMatriculaAtiva = false;

    MatriculaRepositorioEmMemoria comMatriculaExistente() {
        this.existeMatriculaAtiva = true;
        return this;
    }

    MatriculaRepositorioEmMemoria semMatriculaExistente() {
        this.existeMatriculaAtiva = false;
        return this;
    }

    @Override
    public boolean existeMatriculaAtiva(UUID alunoId, PeriodoLetivo periodo) {
        return this.existeMatriculaAtiva;
    }

    @Override
    public Optional<Matricula> buscarPorId(UUID id) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    @Override
    public List<Matricula> buscarPorAluno(UUID alunoId) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    @Override
    public void salvar(Matricula matricula) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }
}
