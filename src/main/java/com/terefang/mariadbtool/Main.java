package com.terefang.mariadbtool;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Slf4j
public class Main extends JFrame implements ActionListener
{
    private JPanel panel;
    private JButton _start;
    private JButton _stop;
    private JButton _exit;
    private JTextArea _textArea;

    static DB _db = null;
    private JScrollPane _scroll;
    private JButton _clean;
    private JSplitPane _split;
    private JPanel _grid;
    private JComboBox<String> _combo;
    private JButton _find;
    private JButton _reset;

    public Main() throws HeadlessException
    {
        super("MariaDB/Tool");
    }

    static int _BHEIGHT = 30;
    static String _MARIADB_DIR_NAME = "_mariadb";
    public void init()
    {
        this.panel = new JPanel();
        this.setBounds(100,100,800,600);
        this.panel.setLayout(new BorderLayout(10,10));
        this.panel.setBounds(80,80,200,200);

        this._clean = new JButton("CleanUp");
        this._clean.setPreferredSize(new Dimension(200, _BHEIGHT));
        this._clean.setActionCommand("clean");
        this._clean.addActionListener(this);
        this.panel.add(this._clean, BorderLayout.PAGE_START);

        this._start = new JButton("Start");
        this._start.setPreferredSize(new Dimension(200, _BHEIGHT));
        this._start.setActionCommand("start");
        this._start.addActionListener(this);
        this.panel.add(this._start, BorderLayout.WEST);

        this._stop = new JButton("Stop");
        this._stop.setPreferredSize(new Dimension(200, _BHEIGHT));
        this._stop.setActionCommand("stop");
        this._stop.addActionListener(this);
        this.panel.add(this._stop, BorderLayout.CENTER);

        this._exit = new JButton("Exit");
        this._exit.setPreferredSize(new Dimension(200, _BHEIGHT));
        this._exit.setActionCommand("exit");
        this._exit.addActionListener(this);
        this.panel.add(this._exit, BorderLayout.EAST);

        this._grid = new JPanel();
        //this._grid.setLayout(new GridLayout(1,3, 3, 3));
        this._combo = new JComboBox<String>();
        this._combo.setPreferredSize(new Dimension(600, _BHEIGHT));
        this._combo.addItem(FileUtils.getTempDirectoryPath()+"/."+ UUID.randomUUID().toString().toUpperCase() +_MARIADB_DIR_NAME);
        this._combo.addItem(FileUtils.getUserDirectoryPath()+"/"+_MARIADB_DIR_NAME);
        restorePathHistory();

        this._grid.add(this._combo);

        this._find = new JButton("...");
        this._find.setSize(new Dimension(50, _BHEIGHT));
        this._find.setActionCommand("find");
        this._find.addActionListener(this);
        this._grid.add(this._find);

        this._reset = new JButton("X");
        this._reset.setSize(new Dimension(50, _BHEIGHT));
        this._reset.setActionCommand("reset");
        this._reset.addActionListener(this);
        this._grid.add(this._reset);


        this.panel.add(this._grid, BorderLayout.PAGE_END);

        this._textArea = new JTextArea(40, 58);
        //this._textArea.setPreferredSize(new Dimension(800, 600));
        this._textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) this._textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this._scroll = new JScrollPane(this._textArea);
        this._scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this._split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this._split.setTopComponent(this.panel);
        this._split.setBottomComponent(this._scroll);

        this.add(this._split);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    static File _cfg = new File(FileUtils.getUserDirectory(), ".mariadb-tool.config");

    @SneakyThrows
    void restorePathHistory()
    {
        if(_cfg.exists())
        {
            FileReader _fh = new FileReader(_cfg);
            LineIterator _it = IOUtils.lineIterator(_fh);
            while(_it.hasNext())
            {
                this._combo.addItem(_it.nextLine());
            }
            IOUtils.close(_fh);
        }
    }

    @SneakyThrows
    void savePathHistory(String _item)
    {
        SortedSet<String> _set = new TreeSet<>();
        if(_cfg.exists())
        {
            FileReader _fh = new FileReader(_cfg);
            LineIterator _it = IOUtils.lineIterator(_fh);
            while(_it.hasNext())
            {
                _set.add(_it.nextLine());
            }
            IOUtils.close(_fh);
        }
        _set.add(_item);
        FileWriter _fw = new FileWriter(_cfg);
        IOUtils.writeLines(_set, "\n", _fw);
        IOUtils.close(_fw);
    }

    @SneakyThrows
    void deletePathHistory(String _item)
    {
        SortedSet<String> _set = new TreeSet<>();
        if(_cfg.exists())
        {
            LineIterator _it = IOUtils.lineIterator(new FileReader(_cfg));
            while(_it.hasNext())
            {
                _set.add(_it.nextLine());
            }
        }
        _set.remove(_item);
        IOUtils.writeLines(_set, "\n", new FileWriter(_cfg));
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        this.log.info(directoryToBeDeleted.getAbsolutePath()+" DELETED");
        return directoryToBeDeleted.delete();
    }

    @Override
    @SneakyThrows
    public void actionPerformed(ActionEvent _ev)
    {
        if("clean".equalsIgnoreCase(_ev.getActionCommand()))
        {
            File _dbdir = new File(this._combo.getSelectedItem().toString());
            _dbdir.mkdirs();
            deleteDirectory(_dbdir);

        }
        else if("start".equalsIgnoreCase(_ev.getActionCommand()))
        {
            new Thread(()->{
                this.log.info("----------------------------------------------------------------------");
                this.log.info("CREATING & STARTING DATABASE, PLEASE WAIT .....");
                this.log.info("----------------------------------------------------------------------");

                File _dbdir = new File(this._combo.getSelectedItem().toString());

                DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
                configBuilder.setPort(6612);
                configBuilder.setDataDir(this._combo.getSelectedItem().toString()+"/data");
                configBuilder.setBaseDir(this._combo.getSelectedItem().toString()+"/base");
                configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
                configBuilder.setSecurityDisabled(true);
                configBuilder.setUnpackingFromClasspath(!(new File(_dbdir,"base").exists()));

                _dbdir.mkdirs();
                try
                {
                    _db = DB.newEmbeddedDB(configBuilder.build());
                    _db.start();

                    this.log.info("DATABASE URL = "+_db.getConfiguration().getURL("dataBaseName"));
                }
                catch (Exception _xe)
                {
                    this.log.error(_xe.getMessage());
                }
            }).start();
        }
        else if("stop".equalsIgnoreCase(_ev.getActionCommand()))
        {
            try
            {
                _db.stop();
            }
            catch (Exception _xe)
            {
                this.log.error(_xe.getMessage());
            }
        }
        else if("exit".equalsIgnoreCase(_ev.getActionCommand()))
        {
            try
            {
                _db.stop();
            }
            catch (Exception _xe)
            {
                this.log.error(_xe.getMessage());
            }
            System.exit(0);
        }
        else if("find".equalsIgnoreCase(_ev.getActionCommand()))
        {
            JFileChooser _j = new JFileChooser();
            _j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int _opt = _j.showSaveDialog(this);
            if(_opt == JFileChooser.APPROVE_OPTION)
            {
                String _path = new File(_j.getSelectedFile(), _MARIADB_DIR_NAME).getAbsolutePath();
                this._combo.addItem(_path);
                this._combo.setSelectedItem(_path);
                savePathHistory(_path);
            }
        }
        else if("reset".equalsIgnoreCase(_ev.getActionCommand()))
        {
            try
            {
                int _sel = this._combo.getSelectedIndex();
                deletePathHistory(this._combo.getSelectedItem().toString());
                this._combo.removeItemAt(_sel);
                this._combo.setSelectedIndex(0);
            }
            catch (Exception _xe) { /* IGNORE */}
        }
    }

    public void logTo(String message)
    {
        this._textArea.append("\n");
        this._textArea.append(message);
        this._textArea.setCaretPosition(this._textArea.getDocument().getLength());
        this._textArea.repaint();
    }

    public static void main(String[] args)
    {

        final Main _main = new Main();

        Logger.getLogger("").addHandler(new Handler() {
            @Override
            public void publish(LogRecord _record) {
                _main.logTo(_record.getMessage());
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });

        _main.init();
    }

}
