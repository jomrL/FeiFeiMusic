package cn.edu.swufe.feifeimusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    private DrawerLayout _drawerLayout;
    private List<Map<String,Object>> song_list;
    private int current_play_position=0;
    private int pos;
    private Toolbar toolbar;
    private String music_duration="";
    public static SeekBar audioSeekBar = null;//定义进度条
    private MusicPlayerService musicPlayerService;
    private int current_play_music;//当前播放的音乐
    private NavigationView navigation;
    private boolean isplay = false;//音乐是否在播放
    int play_time=0;//记录是否是播放过
    private Intent intent;
    private ServiceConnection conn;
    private boolean isExit = false;//返回键
    private MediaPlayer mediaPlayer ;
    private ListView listView;
    private Handler handler;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView btn_play_pause;
    //退出应用再进入时（点击app图标或者在通知栏点击service）使用，判断服务是否在启动
    private boolean isservicerunning = false;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        current_play_music=position;
        play_time=1;
        player(current_play_music);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

//        Log.i("长按", "onItemLongClick: ");
        String current_path=(String)song_list.get(position).get("song_path");;
        Toast.makeText(MainActivity.this,"地址"+current_path,Toast.LENGTH_SHORT).show();
        return true;
    }
    public void requestPower() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                Toast.makeText(MainActivity.this,"你曾经拒绝过权限，请到权限中去设置",Toast.LENGTH_LONG);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                Toast.makeText(MainActivity.this,"你曾经拒绝过权限，请到权限中去设置",Toast.LENGTH_LONG);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) {
                Toast.makeText(this, "读取存储空间" + "权限" + permissions[0] + "申请成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "" + "读取存储空间权限" + permissions[0] + "申请失败", Toast.LENGTH_SHORT).show();
            }

        }
    }
    public void fitListView(){
        //扫描获得所有音乐数据
        List<Song> songs=MusicUtils.getMusicData(MainActivity.this);
//        转化为List<Map<String,Object>>列表
        song_list=new ArrayList<Map<String,Object>>();
        for(Song song1:songs){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("song_name",song1.song);
            map.put("song_singer",song1.singer);
            map.put("song_duration",MusicUtils.formatTime(song1.duration));
            map.put("song_int_duration",song1.duration);
            float size1=Float.parseFloat(String.valueOf(song1.size))/1000000;
            String size_song=String.format("%.2f",size1)+" MB";
            map.put("song_size",size_song);
            map.put("song_path",song1.path);
            song_list.add(map);
        }
        //加载simpleAdapter使用自己定义的my_list做布局文件
        SimpleAdapter adapter=new SimpleAdapter(MainActivity.this,
                song_list,
                R.layout.my_list,
                new String[]{"song_name","song_singer","song_duration","song_size"},
                new int[]{R.id.song_name,R.id.song_singer,R.id.song_duration,R.id.song_size});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPower();
        _drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        toolbar=(Toolbar) findViewById(R.id.mytoolbar);
        listView=(ListView)findViewById(R.id.musicListView);
        navigation=(NavigationView)findViewById(R.id.nav_view);
        btn_play_pause=(ImageView)findViewById(R.id.play_pause);
        audioSeekBar=(SeekBar)findViewById(R.id.seekBar);
        init();
    }

    private void init() {

        intent = new Intent();
        intent.setAction("player");
        intent.setPackage(getPackageName());
        handler = new Handler();
        //设置toolbar标题文本
//        toolbar.setTitle("Player");
        //设置toolbar
        setSupportActionBar(toolbar);
        //设置左上角图标是否可点击以及不显示title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        //左上角加上一个返回图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle=new ActionBarDrawerToggle(MainActivity.this,_drawerLayout,toolbar,
                R.string.open,R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
//                Toast.makeText(MainActivity.this,R.string.open,Toast.LENGTH_SHORT).show();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
//                Toast.makeText(MainActivity.this,R.string.close,Toast.LENGTH_SHORT).show();
                super.onDrawerClosed(drawerView);
            }
        };
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(MainActivity.this,menuItem.getTitle(),Toast.LENGTH_SHORT).show();
                menuItem.setChecked(true);
                //_drawerLayout.closeDrawers();
                return false;
            }
        });
        drawerToggle.syncState();
        _drawerLayout.addDrawerListener(drawerToggle);

        //ListView数据填充
        fitListView();

        //获取SharePrefrences存入的最后一首播放的音乐,以及播放进度
        SharedPreferences sp=getSharedPreferences("myconfig",MODE_PRIVATE);
        String first_song_path=(String)song_list.get(0).get("song_path");
        pos=sp.getInt("song_progress",0);
        audioSeekBar.setProgress(pos);
        current_play_music=findIdByPath(sp.getString("song_path",first_song_path));
        //设置musicinfo
        TextView textView=(TextView)findViewById(R.id.musicinfo);
        textView.setText((String)song_list.get(current_play_music).get("song_name"));
        textView=(TextView)findViewById(R.id.musictime);
        music_duration=(String)song_list.get(current_play_music).get("song_duration");

        listView.setOnItemClickListener(MainActivity.this);
        audioSeekBar.setMax((int)song_list.get(current_play_music).get("song_int_duration"));
        textView.setText(MusicUtils.formatTime(pos)+"/"+music_duration);
        //退出后再次进去程序时，进度条保持持续更新
        if(MusicPlayerService.mediaPlayer!=null){
            reinit();//更新页面布局以及变量相关
        }
        //播放进度监听 ，使用静态变量时别忘了Service里面还有个进度条刷新
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pos=progress;
                if (current_play_music == -1) {

                    Log.i("MusicPlayerService", "MusicActivity...showInfo(请选择要播放的音乐);.........");
                    //还没有选择要播放的音乐
                    showInfo("请选择要播放的音乐");
                } else {
                    //假设改变源于用户拖动
                    if (fromUser) {
                        if(play_time==0){
                            player(current_play_music);
                            isplay=false;
                            btn_play_pause.setBackgroundResource(R.drawable.play);

                        }else{
                            //这里有个问题，如果播放时用户拖进度条还好说，但是如果是暂停时，拖完会自动播放，所以还需要把图标设置一下
                            Log.i("进度条", "onProgressChanged: "+progress);
                            MusicPlayerService.mediaPlayer.seekTo(progress);// 当进度条的值改变时，音乐播放器从新的位置开始播放
                            //如果当前是未播放状态，暂停播放
                        }
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                btn_play_pause.setBackgroundResource(R.drawable.play);
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }

//                MusicPlayerService.mediaPlayer.pause(); // 开始拖动进度条时，音乐暂停播放
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(play_time==1){
                    if (isplay==true) {
                        btn_play_pause.setBackgroundResource(R.drawable.pause);
                        mediaPlayer.start();
                    }
                }else {
                    player(current_play_music);

                    isplay=true;
                    btn_play_pause.setBackgroundResource(R.drawable.pause);
                }
            }
        });
    }
    private int findIdByPath(String path){
        int pos=0;
        for(Map<String,Object> map:song_list){
            String path1=(String)map.get("song_path");
            if(path.equals(path1)){
                break;
            }else{
                pos+=1;
            }
        }
        return pos;
    }

    private void reinit() {
        //设置进度条最大值
//        audioSeekBar.setMax(MusicPlayerService.mediaPlayer.getDuration());
//        audioSeekBar.setProgress(MusicPlayerService.mediaPlayer.getCurrentPosition());
//        currentposition = MusicPlayerService.getCurposition();
        Log.i("MusicPlayerService","reinit.........");
        isservicerunning = true;
        //如果是正在播放
        if(MusicPlayerService.mediaPlayer.isPlaying()){
            isplay = true;
            btn_play_pause.setBackgroundResource(R.drawable.pause);
        }
        conn = new ServiceConnection() {
            /**
             * 获取服务对象时的操作
             */
            public void onServiceConnected(ComponentName name, IBinder service) {
                // TODO Auto-generated method stub
                musicPlayerService = ((MusicPlayerService.musicBinder) service).getPlayInfo();
                mediaPlayer = musicPlayerService.getMediaPlayer();
                Log.i("MusicPlayerService", "MusicActivity...onServiceConnected.......");
                current_play_music = musicPlayerService.getCurposition();
                Log.i("getCurposition", "onServiceConnected: "+current_play_music);
                audioSeekBar.setMax(mediaPlayer.getDuration());
                Log.i("音乐时间", "onServiceConnected: "+mediaPlayer.getDuration());
                Log.i("$$$", "player: "+pos);
                audioSeekBar.setProgress(pos);
                musicPlayerService.setCurrentPosition(pos);
                mediaPlayer.seekTo(pos);
                //设置进度条最大值
                //使用runnable + handler
                handler.post(seekBarHandler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayerService = null;
            }
        };
        //重新绑定service
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void showInfo(String info) {
        Toast.makeText(this,info,Toast.LENGTH_SHORT).show();
    }


        //加载侧滑菜单


    //1s更新一次进度条
    Runnable seekBarThread = new Runnable() {
        @Override
        public void run() {
            while (musicPlayerService != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.i("MusicPlayerService", "seekBarThread run.......");

                audioSeekBar.setProgress(musicPlayerService.getCurrentPosition());

            }
        }
    };
    Runnable seekBarHandler = new Runnable() {
        @Override
        public void run() {

            //Log.i("MusicPlayerService", "MusicActivity...seekBarHandler run......."+mediaPlayer.getCurrentPosition()+" "+mediaPlayer.getDuration());
            audioSeekBar.setProgress(musicPlayerService.getCurrentPosition());
            TextView textView=(TextView)findViewById(R.id.musictime);
            textView.setText(MusicUtils.formatTime(mediaPlayer.getCurrentPosition())+"/"+music_duration);
            handler.postDelayed(seekBarHandler, 1000);

        }
    };

    public void previous(View view){
        if(current_play_music > 0){
            current_play_music -= 1;
            audioSeekBar.setProgress(0);
//            musicPlayerService.setCurrentPosition(0);
            player();
        }else{
            showInfo("已经是第一首音乐了");
        }
    }
    public void previous(){
        if(current_play_music > 0){
            current_play_music -= 1;
            audioSeekBar.setProgress(0);
//            musicPlayerService.setCurrentPosition(0);
            player();

        }else{
            showInfo("已经是第一首音乐了");
        }
    }
    public void next(){
        if(current_play_music < song_list.size()-2){
            play_time=1;
            current_play_music += 1;
            //musicPlayerService.setCurrentPosition(0);
            player();
        }else{
            showInfo("已经是最后一首音乐了");
        }
    }
    public void next(View view){
        if(current_play_music < song_list.size()-2){
            current_play_music += 1;
            audioSeekBar.setProgress(0);
//            musicPlayerService.setCurrentPosition(0);
            player();
        }else{
            showInfo("已经是最后一首音乐了"); }


    }
    public void play_pause(View view){
        Log.i("my play time is", "play_pause:!!!!!!!!!!!!!!! "+play_time);
        if(play_time==1){//并不是第一次点击
            if(isplay==true){//暂停
                isplay=false;
                current_play_position=musicPlayerService.getCurrentPosition();
                btn_play_pause.setBackgroundResource(R.drawable.play);
                mediaPlayer.pause();
            }
            else{
                isplay = true;
                btn_play_pause.setBackgroundResource(R.drawable.pause);
                musicPlayerService.getMediaPlayer().start();
            }

        }
        else{//第一次点击，默认打开最后一次播放的音乐，如果没有，打开第一首音乐
            player(current_play_music);
            isplay=true;
            btn_play_pause.setBackgroundResource(R.drawable.pause);

        }

    }
    private  void player(String info){

        intent.putExtra("MSG",info);
        isplay = true;
        btn_play_pause.setBackgroundResource(R.drawable.pause);
        startService(intent);

    }
    private void player() {
        player(current_play_music);
    }

    private void player(int position){
        TextView textView=(TextView)findViewById(R.id.musicinfo);
        textView.setText((String)song_list.get(position).get("song_name"));
        textView=(TextView)findViewById(R.id.musictime);
        music_duration=(String)song_list.get(position).get("song_duration");
        textView.setText("0:00/"+music_duration);
        Log.i("my play time is", "player: "+play_time);
        intent.putExtra("curposition", position);//把位置传回去，方便再启动时调用
        intent.putExtra("url", (String)song_list.get(position).get("song_path"));
        intent.putExtra("MSG","0");
        isplay = true;

        btn_play_pause.setBackgroundResource(R.drawable.pause);

        startService(intent);
        Log.i("player", "player: "+play_time);
        conn = new ServiceConnection() {
            /**
             * 获取服务对象时的操作
             */
            public void onServiceConnected(ComponentName name, IBinder service) {
                // TODO Auto-generated method stub
                musicPlayerService = ((MusicPlayerService.musicBinder) service).getPlayInfo();
                mediaPlayer = musicPlayerService.getMediaPlayer();
                Log.i("MusicPlayerService", "MusicActivity...onServiceConnected.......");
                current_play_music = musicPlayerService.getCurposition();
                Log.i("getCurposition", "onServiceConnected: "+current_play_music);
                audioSeekBar.setMax(mediaPlayer.getDuration());
                Log.i("音乐时间", "onServiceConnected: "+mediaPlayer.getDuration());
                if(play_time==0){
                    Log.i("$$$", "player: "+pos);
                    audioSeekBar.setProgress(pos);
                    musicPlayerService.setCurrentPosition(pos);
                    mediaPlayer.seekTo(pos);
                    play_time=1;
                }

                //设置进度条最大值
                //使用runnable + handler
                handler.post(seekBarHandler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayerService = null;
            }
        };
        Log.i("my play time is", "play_pause1!!!!!!!!!!!!!: "+play_time);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.i("musicPlayerService", "player: "+musicPlayerService);
        Log.i("MusicPlayerService","MusicActivity...bindService.......");

    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp=getSharedPreferences("myconfig",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("song_path",(String)song_list.get(current_play_music).get("song_path"));
        if(mediaPlayer!=null){
            editor.putInt("song_progress",mediaPlayer.getCurrentPosition());
        }
        editor.commit();
        Log.i("MusicPlayerService", "MusicActivity...onResume........." + Thread.currentThread().hashCode());
        init();
    }

    @Override
    protected void onPause() {

        super.onPause();

        Log.i("MusicPlayerService", "MusicActivity...onPause........." + Thread.currentThread().hashCode());
        //绑定服务了
        if(musicPlayerService != null){
            unbindService(conn);
        }
        handler.removeCallbacks(seekBarHandler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //用SharePrefrence存储最后一次播放的音乐地址
        SharedPreferences sp=getSharedPreferences("myconfig",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("song_path",(String)song_list.get(current_play_music).get("song_path"));
        if(mediaPlayer!=null){
            editor.putInt("song_progress",mediaPlayer.getCurrentPosition());
        }
        editor.commit();
//        unbindService(conn);
        Log.i("MusicPlayerService", "MusicActivity...onDestroy........." + Thread.currentThread().hashCode());
    }
    private void exit(String info) {
        if(!isExit) {
            isExit = true;
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
        }
    }
    //按两次返回键退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            //音乐服务启动了，隐藏至通知栏
            if(musicPlayerService != null){
                exit("再按一次隐藏至通知栏");
            }else{
                exit("再按一次退出程序");
            }

        }
        return false;
    }

}
