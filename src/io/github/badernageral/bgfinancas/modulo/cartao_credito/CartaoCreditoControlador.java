/*
Copyright 2012-2018 Jose Robson Mariano Alves

This file is part of bgfinancas.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This package is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

package io.github.badernageral.bgfinancas.modulo.cartao_credito;

import io.github.badernageral.bgfinancas.biblioteca.ajuda.Ajuda;
import io.github.badernageral.bgfinancas.biblioteca.contrato.Controlador;
import io.github.badernageral.bgfinancas.biblioteca.utilitario.Animacao;
import io.github.badernageral.bgfinancas.biblioteca.sistema.Kernel;
import io.github.badernageral.bgfinancas.biblioteca.sistema.Tabela;
import io.github.badernageral.bgfinancas.biblioteca.utilitario.Validar;
import io.github.badernageral.bgfinancas.biblioteca.sistema.Janela;
import io.github.badernageral.bgfinancas.biblioteca.tipo.Duracao;
import io.github.badernageral.bgfinancas.biblioteca.tipo.Posicao;
import io.github.badernageral.bgfinancas.biblioteca.tipo.Status;
import io.github.badernageral.bgfinancas.biblioteca.utilitario.Erro;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import io.github.badernageral.bgfinancas.template.cena.CenaPadrao;
import io.github.badernageral.bgfinancas.modelo.CartaoCredito;

public final class CartaoCreditoControlador implements Initializable, Controlador {
       
    private final String TITULO = idioma.getMensagem("cartoes_credito");
    
    @FXML private CenaPadrao cenaController;
    private ObservableList<CartaoCredito> itens;
    private final Tabela<CartaoCredito> tabela = new Tabela<>();
    private final TableView<CartaoCredito> tabelaLista = new TableView<>();;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Kernel.setTitulo(TITULO);
        cenaController.setTitulo(TITULO);
        cenaController.setTabela(tabelaLista);
        tabela.prepararTabela(tabelaLista);
        tabela.adicionarColuna(tabelaLista, idioma.getMensagem("nome"), "nome");
        tabela.adicionarColunaNumero(tabelaLista, idioma.getMensagem("limite"), "limite");
        tabela.setColunaColorida(tabela.adicionarColunaNumero(tabelaLista, idioma.getMensagem("saldo"), "saldo"));
        tabela.adicionarColunaNumero(tabelaLista, idioma.getMensagem("dia_vencimento"), "vencimento");
        tabela.adicionarColuna(tabelaLista, idioma.getMensagem("ativado"), "ativado");
        acaoFiltrar(false);
    }
    
    @Override
    public void acaoFiltrar(Boolean animacao){
        tabelaLista.setItems(new CartaoCredito().getRelatorio(cenaController.getFiltro().getText()));
        if(animacao){
            Animacao.fadeOutIn(tabelaLista);
        }
    }
    
    @Override
    public void acaoCadastrar(int botao) {
        CartaoCreditoFormularioControlador controlador = Janela.abrir(CartaoCredito.FXML_FORMULARIO, TITULO);
        controlador.cadastrar(null);
    }
    
    @Override
    public void acaoAlterar(int tabela) {
        itens = tabelaLista.getSelectionModel().getSelectedItems();
        if(Validar.alteracao(itens, cenaController.getBotaoAlterar())){
            CartaoCreditoFormularioControlador Controlador = Janela.abrir(CartaoCredito.FXML_FORMULARIO, TITULO);
            Controlador.alterar(itens.get(0));
        }
    }
    
    @Override
    public void acaoExcluir(int botao) {
        itens = tabelaLista.getSelectionModel().getSelectedItems();
        if(Validar.exclusao(itens, cenaController.getBotaoExcluir())){
            try{
                for(CartaoCredito cc : itens){
                    cc.excluir();
                }
                Janela.showTooltip(Status.SUCESSO, idioma.getMensagem("operacao_sucesso"), Duracao.CURTA);
            }catch(Erro e){
                Janela.showTooltip(Status.ERRO, idioma.getMensagem("restricao_excluir"), Duracao.NORMAL);
            }
            acaoFiltrar(true);
        }
    }

    @Override
    public void acaoGerenciar(int botao) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoVoltar() {
        Kernel.principal.acaoVoltar();
    }

    @Override
    public void acaoAjuda() {
        Ajuda.getInstance().setObjetos(cenaController,tabelaLista);
        Ajuda.getInstance().capitulo(Posicao.CENTRO, idioma.getMensagem("tuto_cartao_credito_1"));
        Ajuda.getInstance().capitulo(Posicao.CENTRO, idioma.getMensagem("tuto_cartao_credito_2"));
        Ajuda.getInstance().capitulo(cenaController.getBotaoCadastrar(), 
                Posicao.BAIXO, idioma.getMensagem("tuto_cadastrar"));
        Ajuda.getInstance().capitulo(cenaController.getBotaoAlterar(), 
                Posicao.BAIXO, idioma.getMensagem("tuto_alterar"));
        Ajuda.getInstance().capitulo(cenaController.getBotaoExcluir(), 
                Posicao.BAIXO, idioma.getMensagem("tuto_excluir"));
        Ajuda.getInstance().apresentarProximo();
    }
    
}