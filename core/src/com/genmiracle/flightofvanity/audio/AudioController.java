package com.genmiracle.flightofvanity.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Audio;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.instance.PlayerController;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.audio.AudioEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioController {

    private Music currentlyPlaying;

    private Music currVictory;

    private String currPlayingName;

    private float globalMusicLevel = .5f;
    private float globalSoundLevel = .5f;

    private boolean playingFootsteps;

    private int footstepCounter = 1;

    private int jumpCounter = 1;
    private PlayerController pc;
    Audio engine;
    private HashMap<String, Music> musics;

    private ArrayList<String> musicList;

    private HashMap<String, Sound> sounds;
    private HashMap<String, Boolean> soundsPlaying;
    MusicQueue musicBuffer;

    // fade out
    float FACTOR = .07f; // The bigger the factor, the faster the fade-out will be
    float mVolume = 1;
    private boolean playPickup = true;
    private boolean playedDeath = false;

    public AudioController() {
        musics = new HashMap<String, Music>();

        musicList = new ArrayList<String>();
        sounds = new HashMap<String, Sound>();
        engine = Gdx.audio;
        // musicBuffer = engine.newMusicBuffer(false, 44100);
    }

    public void loadAudio() {
        addMusic("Flight", "Flight.wav", true);
        addMusic("Bonfire Waltz", "BonfireWaltz.wav", true);
        addMusic("Bonfire Waltz (Orchestral)", "BonfireWaltzOrchestra.wav", true);
        addMusic("Bonfire Waltz (Jack's Remix)", "BonfireWaltzJack.wav", true);
        addMusic("Lonely Light", "LonelyLight.wav", true);
        addMusic("victory", "victory.wav", false);
        addMusic("victoryJack", "victoryJack.wav", false);
        addSound("death", "death.wav");
        addSound("pickup", "pickup.wav");
        addSound("place", "place.wav");
        addSound("jump1", "jump1.wav");
        addSound("unlock", "unlock.wav");
        addMusic("Daydreaming", "levelSelect.wav", true);

        loadGlobalAudioLevels();
        currVictory = musics.get("victory");

        // addSound("jump2","jump2.wav");
        // addSound("footsteps", "assets/audio/footsteps.wav");
    }

    public void addMusic(String name, String fileName) {
        try {
            musics.put(name, engine.newMusic(Gdx.files.internal("assets/audio/" + fileName)));
        } catch (RuntimeException e) {
            musics.put(name, engine.newMusic(Gdx.files.internal("audio/" + fileName)));
        }
    }

    public void addMusic(String name, String fileName, boolean levelMusic) {
        try {
            musics.put(name, engine.newMusic(Gdx.files.internal("assets/audio/" + fileName)));
            if (levelMusic)
                musicList.add(name);
        } catch (RuntimeException e) {
            musics.put(name, engine.newMusic(Gdx.files.internal("audio/" + fileName)));
            if (levelMusic)
                musicList.add(name);
        }
    }

    public void addSound(String name, String fileName) {
        try {
            sounds.put(name, engine.newSound(Gdx.files.internal("audio/" + fileName)));
        } catch (RuntimeException e) {
            sounds.put(name, engine.newSound(Gdx.files.internal("audio/" + fileName)));
        }
    }

    public void loadAssets(AssetDirectory dir) {
        // try {
        // music = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/test.wav"));
        // } catch (RuntimeException e) {
        // music = Gdx.audio.newMusic(Gdx.files.internal("audio/test.wav"));
        // }
    }

    public void setPlayerController(PlayerController p) {
        pc = p;

    }

    public void playMusic(String name, boolean looping) {

        if (musics != null) {
            if (currVictory != null && currVictory.isPlaying())
                currVictory.dispose();
            if (musics.get(name) != currentlyPlaying) {
                if (currentlyPlaying != null)
                    currentlyPlaying.dispose();
                currentlyPlaying = musics.get(name);
                currPlayingName = name;
                currentlyPlaying.play();
                currentlyPlaying.setVolume(globalMusicLevel);
                currentlyPlaying.setLooping(looping);
            } else
                currentlyPlaying.play();
        }
    }

    public void playSound(String name) {

        if (sounds != null) {
            Sound sound = sounds.get(name);
            sound.play(globalSoundLevel);
            // System.out.println("sound "+globalSoundLevel);
        }
    }

    public void playSound(String name, float volume) {

        if (sounds != null) {
            Sound sound = sounds.get(name);
            sound.play(volume);
        }
    }

    public void update(float delta) {
        Player p = pc.getPlayer();
        if(!p.isDeadAC())
            playedDeath = false;
        death(p);
        // footsteps(p, delta);
        if (p.isPickup() && p.getHolding() != null && playPickup) {
            playPickup = false;
            playSound("pickup");
        } else if (p.getPlaced()) {
            playPickup = true;
            p.setPlaced(false);
            playSound("place");

        }
        if (p.isJumping()) {
            playSound("jump" + jumpCounter, .2f * globalSoundLevel);
            // if(jumpCounter == 1) jumpCounter = 2;
            // else jumpCounter = 1;
        }

    }

    public void footsteps(Player p, float delta) {
        int temp = footstepCounter - 1;
        if (temp == 0)
            temp = 4;
        Music prev = musics.get("footsteps" + temp);
        if (p.getMovement() != 0 && p.isGrounded()) {
            if (!prev.isPlaying()) {

                Music footsteps = musics.get("footsteps" + footstepCounter);
                if (footstepCounter == 4)
                    footstepCounter = 1;
                else
                    footstepCounter++;
                footsteps.setVolume(.2f);
                footsteps.play();

            }
            // }else if(!p.isGrounded() && prev.isPlaying()){
            // mVolume -= delta * FACTOR*prev.getVolume();
            // if (mVolume > 0) {
            // prev.setVolume(mVolume);
            // }
            // else {
            // prev.stop();
            // mVolume = .1f;
            // }
        }
    }

    public void death(Player p) {
        if (p.isDeadAC() && !this.playedDeath) {
            // currentlyPlaying.pause();
            playedDeath = true;
            playSound("death", globalSoundLevel);

        }
    }

    public void setGlobalMusicLevel(float lvl) {
        globalMusicLevel = lvl;
        currentlyPlaying.setVolume(lvl);

        saveGlobalAudioLevels();
    }

    public String getCurrPlayingName() {
        return currPlayingName;
    }

    public void setGlobalSoundLevel(float lvl) {
        globalSoundLevel = lvl;

        saveGlobalAudioLevels();
    }

    public float getGlobalMusicLevel() {
        return globalMusicLevel;
    }

    public float getGlobalSoundLevel() {
        return globalSoundLevel;
    }

    public void playNext() {
        String s = "";
        if (currPlayingName.equals("menu") || currPlayingName.equals("Daydreaming"))
            s = musicList.get(0);
        else
            for (int i = 0; i < musicList.size(); i++) {
                if (currPlayingName.equals(musicList.get(i)))
                    if (i < musicList.size() - 1)
                        s = musicList.get(i + 1);
                    else
                        s = musicList.get(0);

            }
        playMusic(s, true);
    }

    public void saveGlobalAudioLevels() {
        Preferences prefs = Gdx.app.getPreferences("audio");

        prefs.putFloat("music", globalMusicLevel);
        prefs.putFloat("sound", globalSoundLevel);

        prefs.flush();
    }

    public void loadGlobalAudioLevels() {
        Preferences prefs = Gdx.app.getPreferences("audio");

        globalMusicLevel = prefs.getFloat("music", 0.5f);
        globalSoundLevel = prefs.getFloat("sound", 0.5f);
    }

    public Music getCurrPlaying() {
        return currentlyPlaying;
    }

    public Music getCurrVictory() {
        return currVictory;
    }

    public void playVictory(boolean isJack) {
        currentlyPlaying.pause();
        if (currVictory != null)
            currVictory.dispose();
        if (isJack)
            currVictory = musics.get("victoryJack");
        else
            currVictory = musics.get("victory");
        currVictory.play();
        currVictory.setVolume(.8f * globalMusicLevel);

    }

}
