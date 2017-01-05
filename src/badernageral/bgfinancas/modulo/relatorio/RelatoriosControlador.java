/*
Copyright 2012-2017 Jose Robson Mariano Alves

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

package badernageral.bgfinancas.modulo.relatorio;

import badernageral.bgfinancas.biblioteca.ajuda.Ajuda;
import badernageral.bgfinancas.biblioteca.contrato.Categoria;
import badernageral.bgfinancas.biblioteca.contrato.Controlador;
import badernageral.bgfinancas.modelo.Extrato;
import badernageral.bgfinancas.biblioteca.contrato.Grafico;
import badernageral.bgfinancas.biblioteca.sistema.Botao;
import badernageral.bgfinancas.biblioteca.sistema.Janela;
import badernageral.bgfinancas.biblioteca.sistema.Kernel;
import badernageral.bgfinancas.biblioteca.sistema.Tabela;
import badernageral.bgfinancas.biblioteca.tipo.Posicao;
import badernageral.bgfinancas.biblioteca.tipo.Status;
import badernageral.bgfinancas.idioma.Linguagem;
import badernageral.bgfinancas.modelo.CartaoCredito;
import badernageral.bgfinancas.modelo.Conta;
import badernageral.bgfinancas.modelo.Despesa;
import badernageral.bgfinancas.modelo.DespesaCategoria;
import badernageral.bgfinancas.modelo.Receita;
import badernageral.bgfinancas.modelo.Transferencia;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public final class RelatoriosControlador implements Initializable, Controlador {
    
    private final String TITULO = idioma.getMensagem("relatorios");
    
    @FXML private Button voltar;
    @FXML private Label labelRelatorio;
    @FXML private Label labelContaCartao;
    @FXML private Label labelInicio;
    @FXML private Label labelFim;
    @FXML private ComboBox<String> relatorio;
    @FXML private ComboBox<String> tipo;
    @FXML private ComboBox<Categoria> listaContaCartao;
    @FXML private DatePicker inicio;
    @FXML private DatePicker fim;
    @FXML private Button atualizar;
    @FXML private Button imprimir;
    @FXML private GridPane tabela;
    @FXML private GridPane barraSuperior;
    
    private BigDecimal valorTotal;
    
    private final NumberAxis xAxisPrincipal = new NumberAxis();
    private final CategoryAxis yAxisPrincipal = new CategoryAxis();
    private final BarChart<String,Number> graficoPrincipal = new BarChart<>(yAxisPrincipal,xAxisPrincipal);
    
    private final NumberAxis xAxisSecundario = new NumberAxis();
    private final CategoryAxis yAxisSecundario = new CategoryAxis();
    private final BarChart<String,Number> graficoSecundario = new BarChart<>(yAxisSecundario,xAxisSecundario);
    
    private final CategoryAxis xAxisLinhas = new CategoryAxis();
    private final NumberAxis yAxisLinhas = new NumberAxis();
    private final LineChart<String,Number> graficoLinhas = new LineChart<>(xAxisLinhas,yAxisLinhas);
    
    private final ListView<DespesaCategoria> listaCategorias = new ListView<>();
    
    private final TableView<Despesa> listaDespesas = new TableView<>();
    private final Tabela<Despesa> tabelaDespesas = new Tabela<>();
    
    private final TableView<Extrato> listaExtrato = new TableView<>();
    private final Tabela<Extrato> tabelaExtrato = new Tabela<>();
                   
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Kernel.setTitulo(TITULO);
        Botao.prepararBotaoVoltar(voltar);
        labelRelatorio.setText(idioma.getMensagem("selecione_relatorio")+":");
        List<String> tipoRelatorio = Arrays.asList(idioma.getMensagem("despesas"),idioma.getMensagem("lista_despesas"),idioma.getMensagem("despesas_tempo"),idioma.getMensagem("despesas_agendadas"),idioma.getMensagem("receitas"),idioma.getMensagem("transferencias"),idioma.getMensagem("extrato"));
        relatorio.setItems(FXCollections.observableList(tipoRelatorio));
        relatorio.getSelectionModel().select(0);
        labelInicio.setText(idioma.getMensagem("inicio")+":");
        labelFim.setText(idioma.getMensagem("fim")+":");
        LocalDate hoje = LocalDate.now();
        inicio.setValue(hoje.withDayOfMonth(1));
        fim.setValue(hoje.withDayOfMonth(hoje.lengthOfMonth()));
        prepararTabelas();
        prepararTipoTempo();
        prepararFiltro();
        imprimir.setVisible(false);
    }
    
    private void prepararTabelas(){
        tabelaDespesas.prepararTabela(listaDespesas);
        tabelaDespesas.adicionarColuna(listaDespesas, idioma.getMensagem("categoria"), "nomeCategoria");
        tabelaDespesas.adicionarColuna(listaDespesas, idioma.getMensagem("item"), "nomeItem");
        tabelaDespesas.adicionarColunaNumero(listaDespesas, idioma.getMensagem("quantidade"), "quantidade");
        tabelaDespesas.adicionarColunaNumero(listaDespesas, idioma.getMensagem("valor"), "valor");
        tabelaExtrato.prepararTabela(listaExtrato);
        TableColumn<Extrato,String> tipo = tabelaExtrato.adicionarColuna(listaExtrato, idioma.getMensagem("tipo"), "tipo");
        tabelaExtrato.adicionarColunaDataHora(listaExtrato, idioma.getMensagem("data"), "dataHora");
        tabelaExtrato.adicionarColuna(listaExtrato, idioma.getMensagem("categoria"), "nomeCategoria");
        tabelaExtrato.adicionarColuna(listaExtrato, idioma.getMensagem("item"), "nomeItem");
        tabelaExtrato.adicionarColunaNumero(listaExtrato, idioma.getMensagem("valor"), "valor");
        TableColumn<Extrato,String> status = tabelaExtrato.adicionarColuna(listaExtrato, idioma.getMensagem("status"), "status");
        status.setCellFactory(e -> new TableCell<Extrato, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item==null ? null : item);
                if (item != null) {
                    setTextFill(item.equals(idioma.getMensagem("agendado")) ? Color.RED : Color.BLACK);
                }
            }
        });
        tipo.setCellFactory(e -> new TableCell<Extrato, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item==null ? null : item);
                if (item != null) {
                    setTextFill(item.equals(idioma.getMensagem("despesa")) ? Color.GREEN : Color.BLUE);
                }
            }
        });
    }
    
    public void acaoImprimir() {
        // a ser implementado
    }
    
    private void prepararTipoTempo(){
        tipo.getItems().add(idioma.getMensagem("semanal"));
        tipo.getItems().add(idioma.getMensagem("mensal"));
        tipo.getItems().add(idioma.getMensagem("anual"));
        tipo.getSelectionModel().select(1);
        tipo.setOnAction(e -> { carregarRelatorio(); });
    }
    
    public void prepararFiltro(){
        barraSuperior.getChildren().remove(atualizar);
        barraSuperior.getChildren().remove(tipo);
        if(relatorio.getSelectionModel().getSelectedItem().equals(idioma.getMensagem("despesas_tempo"))){
            barraSuperior.add(tipo, 9, 0);
            barraSuperior.add(atualizar, 10, 0);
        }else{
            barraSuperior.add(atualizar, 9, 0);
        }
        listaContaCartao.setOnAction(null);
        if(relatorio.getSelectionModel().getSelectedItem().equals(idioma.getMensagem("despesas_agendadas"))){
            labelContaCartao.setText(idioma.getMensagem("cartao_credito")+":");
            new CartaoCredito().montarSelectCategoria(listaContaCartao);
            CartaoCredito cartaoSemCartao = new CartaoCredito().setNome(idioma.getMensagem("sem_cartao_credito"));
            CartaoCredito cartaoSomenteCartao = new CartaoCredito().setNome(idioma.getMensagem("somente_cartao_credito"));
            CartaoCredito cartaoTodos = new CartaoCredito().setNome(idioma.getMensagem("todos"));
            listaContaCartao.getItems().add(cartaoSemCartao);
            listaContaCartao.getItems().add(cartaoSomenteCartao);
            listaContaCartao.getItems().add(cartaoTodos);
            listaContaCartao.getSelectionModel().select(cartaoTodos);
        }else{
            labelContaCartao.setText(idioma.getMensagem("conta")+":");
            new Conta().montarSelectCategoria(listaContaCartao);
            Conta contasTodas = new Conta().setNome(idioma.getMensagem("todas"));
            listaContaCartao.getItems().add(contasTodas);
            listaContaCartao.getSelectionModel().select(contasTodas);
        }
        listaContaCartao.setOnAction(e -> { carregarRelatorio(); });
        carregarRelatorio();
    }
    
    public void carregarRelatorio(){
        removerGraficos();
        switch(relatorio.getSelectionModel().getSelectedIndex()){
            case 0:
                relatorioGraficoBarras(new Despesa().setAgendada("0"));
                break;
            case 1:
                relatorioListaDespesas();
                break;
            case 2:
                relatorioGraficoLinhas();
                break;
            case 3:
                relatorioGraficoBarras(new Despesa().setAgendada("1"));
                break;
            case 4:
                relatorioGraficoBarras(new Receita());
                break;
            case 5:
                relatorioGraficoBarras(new Transferencia());
                break;
            case 6:
                relatorioExtrato();
                break;
            default:
                Janela.showMensagem(Status.ERRO, idioma.getMensagem("nao_encontrado"));
        }
    }
    
    private void removerGraficos(){
        tabela.getChildren().remove(graficoPrincipal);
        tabela.getChildren().remove(graficoSecundario);
        tabela.getChildren().remove(graficoLinhas);
        tabela.getChildren().remove(listaCategorias);
        tabela.getChildren().remove(listaDespesas);
        tabela.getChildren().remove(listaExtrato);
    }
    
    private String getValorTotal(ObservableList<Series<String,Number>> series){
        valorTotal = new BigDecimal("0.0");
        series.stream().forEach((serie) -> {
            serie.getData().stream().forEach((item) -> {
                valorTotal = valorTotal.add(new BigDecimal(item.getYValue().toString()));
            });
        });
        return idioma.getMensagem("moeda")+" "+valorTotal;
    }
    
    private Integer getTipoCategoria(){
        if(relatorio.getSelectionModel().getSelectedItem().equals(idioma.getMensagem("despesas_agendadas"))){
            if(listaContaCartao.getSelectionModel().getSelectedItem().getNome().equals(idioma.getMensagem("somente_cartao_credito"))){
                return 4;
            }else if(listaContaCartao.getSelectionModel().getSelectedItem().getNome().equals(idioma.getMensagem("sem_cartao_credito"))){
                return 3;
            }else{
                return 2;
            }
        }
        return 1;
    }
    
    private void relatorioListaDespesas(){
        tabela.add(listaDespesas, 0, 1);
        GridPane.setColumnSpan(listaDespesas, GridPane.REMAINING);
        listaDespesas.setItems(new Despesa().listarPeriodoAgrupado(inicio.getValue(), fim.getValue()));
    }
    
    private void relatorioExtrato(){
        tabela.add(listaExtrato, 0, 1);
        GridPane.setColumnSpan(listaExtrato, GridPane.REMAINING);
        ObservableList<Extrato> despesas = new Despesa().getExtrato(inicio.getValue(), fim.getValue());
        ObservableList<Extrato> receitas = new Receita().getExtrato(inicio.getValue(), fim.getValue());
        listaExtrato.setItems(despesas);
        listaExtrato.getItems().addAll(receitas);
        listaExtrato.getItems().sort((Extrato a, Extrato b) -> {
            return a.getDataHora().compareTo(b.getDataHora());
        });
    }
    
    private void relatorioGraficoBarras(Grafico objeto){
        xAxisPrincipal.setLabel(idioma.getMensagem("valores")+" ("+idioma.getMensagem("moeda")+")");
        xAxisSecundario.setLabel(idioma.getMensagem("valores")+" ("+idioma.getMensagem("moeda")+")");
        ajustarColunas(50,50);
        tabela.add(graficoPrincipal, 0, 1);
        tabela.add(graficoSecundario, 1, 1);
        String id_categoria = listaContaCartao.getSelectionModel().getSelectedItem().getIdCategoria();
        Integer tipo_categoria = getTipoCategoria();
        graficoPrincipal.getData().setAll(objeto.getRelatorioMensalBarras(inicio.getValue(), fim.getValue(), null, id_categoria, tipo_categoria));
        graficoPrincipal.getData().stream().forEach((serie) -> {
            serie.getData().stream().forEach((item) -> {
                item.getNode().setCursor(Cursor.HAND);
                item.getNode().setOnMouseClicked((MouseEvent event) -> {
                    updateGraficoSecundario(serie.getName(), objeto);
                });
                eventosGrafico(item.getNode(), serie.getName()+" - "+idioma.getMensagem("moeda")+" "+item.getYValue());
            });
        });
        graficoPrincipal.setTitle(idioma.getMensagem("categorias")+" - "+getValorTotal(graficoPrincipal.getData()));
        graficoPrincipal.lookupAll("Label.chart-legend-item").stream().forEach((Node legenda) -> {
            legenda.setCursor(Cursor.HAND);
            legenda.setOnMouseClicked((MouseEvent event) -> {
                if(legenda instanceof Label){
                    Label texto = (Label) legenda;
                    updateGraficoSecundario(texto.getText(), objeto);
                }
            });
        });
        try{
            Series<String,Number> primeiraCategoria = graficoPrincipal.getData().get(0);
            updateGraficoSecundario(primeiraCategoria.getName(), objeto);
        }catch(Exception e){
            updateGraficoSecundario("", null);
        }
    }
    
    private void updateGraficoSecundario(String nomeCategoria, Grafico objeto){
        if(objeto!=null){
            String id_categoria = listaContaCartao.getSelectionModel().getSelectedItem().getIdCategoria();
            Integer tipo_categoria = getTipoCategoria();
            graficoSecundario.getData().setAll(objeto.getRelatorioMensalBarras(inicio.getValue(), fim.getValue(), nomeCategoria, id_categoria, tipo_categoria));
            graficoSecundario.getData().stream().forEach((serie) -> {
                serie.getData().stream().forEach((item) -> {
                    eventosGrafico(item.getNode(), serie.getName()+" - "+idioma.getMensagem("moeda")+" "+item.getYValue());
                });
            });
            graficoSecundario.setTitle(idioma.getMensagem("itens")+" / "+nomeCategoria+" - "+getValorTotal(graficoSecundario.getData()));
        }else{
            graficoSecundario.getData().clear();
            graficoSecundario.setTitle(idioma.getMensagem("itens")+" - "+getValorTotal(graficoSecundario.getData()));
        }
    }
    
    private void relatorioGraficoLinhas(){   
        listaCategorias.setEditable(true);
        if(listaCategorias.getItems().size()<=0){
            ObservableList<DespesaCategoria> categorias = new DespesaCategoria().listar();
            listaCategorias.setItems(categorias);
        }
        listaCategorias.setCellFactory(CheckBoxListCell.forListView((DespesaCategoria param) -> {
            BooleanProperty observable = param.getSelecao();
            observable.addListener((obs, wasSelected, isNowSelected) -> gerarGraficoLinhas() );
            return observable;
        }));
        ajustarColunas(25,75);
        tabela.add(listaCategorias, 0, 1);
        tabela.add(graficoLinhas, 1, 1);  
        gerarGraficoLinhas();
    }
    
    public void gerarGraficoLinhas(){
        List<String> categoriasSelecionadas = new ArrayList<>();
        listaCategorias.getItems().stream().forEach((categoria) -> {
            if(categoria.isSelecionado()){
                categoriasSelecionadas.add(categoria.getIdCategoria());
            }
        });
        new Despesa().preencherRelatorioMensalLinhas(graficoLinhas, inicio.getValue(), fim.getValue(), categoriasSelecionadas, listaContaCartao.getSelectionModel().getSelectedItem(), tipo.getSelectionModel().getSelectedItem());
        graficoLinhas.getData().stream().forEach((serie) -> {
            serie.getData().stream().forEach((item) -> {
                eventosGrafico(item.getNode(), serie.getName()+" - "+idioma.getMensagem("moeda")+" "+item.getYValue());
            });
        });
    }
    
    private void ajustarColunas(int coluna1, int coluna2){
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(coluna1);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(coluna2);
        tabela.getColumnConstraints().setAll(col1,col2);
    }
    
    private void eventosGrafico(Node node, String nome){
        Tooltip tooltip = new Tooltip(nome);
        node.setOnMouseEntered((MouseEvent event) -> {
            tooltip.show(node, event.getScreenX()+15, event.getScreenY());
        });
        node.setOnMouseExited((MouseEvent event) -> {
            tooltip.hide();
        });
    }

    @Override
    public void acaoVoltar() {
        Kernel.principal.acaoVoltar();
    }

    @Override
    public void acaoCadastrar(int botao) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoAlterar(int tabela) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoExcluir(int botao) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoGerenciar(int botao) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoFiltrar(Boolean animacao) {
        System.out.println(idioma.getMensagem("nao_implementado"));
    }

    @Override
    public void acaoAjuda() {
        Ajuda.getInstance().setObjetos(voltar,labelRelatorio,relatorio,labelContaCartao,listaContaCartao,labelInicio,inicio,labelFim,fim,atualizar,graficoPrincipal,graficoSecundario,listaCategorias,graficoLinhas,listaDespesas,listaExtrato);
        Ajuda.getInstance().capitulo(Posicao.CENTRO, idioma.getMensagem("tuto_relat_1"));
        Ajuda.getInstance().capitulo(relatorio, Posicao.BAIXO, idioma.getMensagem("tuto_relat_2"));
        Ajuda.getInstance().capitulo(listaContaCartao, Posicao.BAIXO, idioma.getMensagem("tuto_relat_3"));
        Ajuda.getInstance().capitulo(inicio, Posicao.BAIXO, idioma.getMensagem("tuto_relat_4"));
        Ajuda.getInstance().capitulo(fim, Posicao.BAIXO, idioma.getMensagem("tuto_relat_5"));
        Ajuda.getInstance().capitulo(atualizar, Posicao.BAIXO, idioma.getMensagem("tuto_relat_6"));
        Ajuda.getInstance().apresentarProximo();
    }
    
}
