package com.terefang.mariadbtool;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

    public Main() throws HeadlessException
    {
        super("MariaDB/Tool");
    }

    public void init()
    {
        this.panel = new JPanel();
        this.setBounds(100,100,800,600);
        this.panel.setLayout(new BorderLayout(10,10));
        this.panel.setBounds(80,80,200,200);

        this._clean = new JButton("CleanUp");
        this._clean.setPreferredSize(new Dimension(200, 50));
        this._clean.setActionCommand("clean");
        this._clean.addActionListener(this);
        this.panel.add(this._clean, BorderLayout.PAGE_START);

        this._start = new JButton("Start");
        this._start.setPreferredSize(new Dimension(200, 50));
        this._start.setActionCommand("start");
        this._start.addActionListener(this);
        this.panel.add(this._start, BorderLayout.WEST);

        this._stop = new JButton("Stop");
        this._stop.setPreferredSize(new Dimension(200, 50));
        this._stop.setActionCommand("stop");
        this._stop.addActionListener(this);
        this.panel.add(this._stop, BorderLayout.CENTER);

        this._exit = new JButton("Exit");
        this._exit.setPreferredSize(new Dimension(200, 50));
        this._exit.setActionCommand("exit");
        this._exit.addActionListener(this);
        this.panel.add(this._exit, BorderLayout.EAST);

        this._textArea = new JTextArea(40, 58);
        //this._textArea.setPreferredSize(new Dimension(800, 600));
        this._textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) this._textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this._scroll = new JScrollPane(this._textArea);
        this._scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.panel.add(this._scroll, BorderLayout.PAGE_END);

        this.add(this.panel);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
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
            File _dbdir = new File(FileUtils.getUserDirectory(), "_mariadb");
            _dbdir.mkdirs();
            deleteDirectory(_dbdir);

        }
        else
        if("start".equalsIgnoreCase(_ev.getActionCommand()))
        {
            new Thread(()->{
                this.log.info("----------------------------------------------------------------------");
                this.log.info("CREATING & STARTING DATABASE, PLEASE WAIT .....");
                this.log.info("----------------------------------------------------------------------");

                File _dbdir = new File(FileUtils.getUserDirectory(), "_mariadb");

                DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
                configBuilder.setPort(6612);
                configBuilder.setDataDir(_dbdir.getAbsolutePath()+"/data");
                configBuilder.setBaseDir(_dbdir.getAbsolutePath()+"/base");
                configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
                configBuilder.setSecurityDisabled(true);
                configBuilder.setUnpackingFromClasspath(!_dbdir.exists());

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
        else
        if("stop".equalsIgnoreCase(_ev.getActionCommand()))
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
        else
        if("exit".equalsIgnoreCase(_ev.getActionCommand()))
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
