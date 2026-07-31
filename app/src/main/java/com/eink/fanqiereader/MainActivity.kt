package com.eink.fanqiereader

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectEInkEngine(view)
            }
        }

        webView.loadUrl("https://fanqienovel.com")
    }

    private fun injectEInkEngine(view: WebView?) {
        val js = """
        (function() {
            if (window.einkInjected) return;
            window.einkInjected = true;

            var style = document.createElement('style');
            style.innerHTML = `
                * {
                    background-color: #ffffff !important;
                    color: #000000 !important;
                    animation: none !important;
                    transition: none !important;
                    box-shadow: none !important;
                    text-shadow: none !important;
                }
                .eink-zone-left {
                    position: fixed; top: 0; left: 0; width: 30%; height: 100%; z-index: 9998;
                }
                .eink-zone-right {
                    position: fixed; top: 0; right: 0; width: 30%; height: 100%; z-index: 9998;
                }
                #eink-btn {
                    position: fixed; bottom: 15px; right: 15px; z-index: 10000;
                    background: #000 !important; color: #fff !important;
                    padding: 8px 14px; border-radius: 4px; font-size: 14px; font-weight: bold;
                }
                #eink-panel {
                    display: none; position: fixed; bottom: 60px; right: 15px; z-index: 10000;
                    background: #fff !important; border: 2px solid #000; padding: 14px;
                    width: 230px; font-size: 13px;
                }
                #eink-panel label { display: block; margin-top: 8px; font-weight: bold; }
                #eink-panel input { width: 100%; margin-top: 4px; }
            `;
            document.head.appendChild(style);

            var readerStyle = document.createElement('style');
            document.head.appendChild(readerStyle);

            function applySettings() {
                var fontSize = localStorage.getItem('eink_fs') || '22';
                var lineHeight = localStorage.getItem('eink_lh') || '1.6';
                var paraSpacing = localStorage.getItem('eink_ps') || '14';
                var padding = localStorage.getItem('eink_pd') || '16';

                readerStyle.innerHTML = `
                    article, p, .reader-content, body {
                        font-size: \${fontSize}px !important;
                        line-height: \${lineHeight} !important;
                        padding-left: \${padding}px !important;
                        padding-right: \${padding}px !important;
                    }
                    p {
                        margin-bottom: \${paraSpacing}px !important;
                    }
                `;
            }
            applySettings();

            var panel = document.createElement('div');
            panel.id = 'eink-panel';
            panel.innerHTML = `
                <div style="font-weight:bold;margin-bottom:8px;border-bottom:1px solid #000;padding-bottom:4px;">墨案排版设置</div>
                <label>字号大小 (px)</label>
                <input type="range" id="eink-fs" min="14" max="38" value="\${localStorage.getItem('eink_fs')||22}">
                
                <label>行间距</label>
                <input type="range" id="eink-lh" min="1.0" max="2.5" step="0.1" value="\${localStorage.getItem('eink_lh')||1.6}">
                
                <label>段落间距 (px)</label>
                <input type="range" id="eink-ps" min="0" max="35" value="\${localStorage.getItem('eink_ps')||14}">
                
                <label>左右边距 (px)</label>
                <input type="range" id="eink-pd" min="0" max="40" value="\${localStorage.getItem('eink_pd')||16}">
                
                <button id="eink-close" style="margin-top:12px;width:100%;background:#000!important;color:#fff!important;padding:6px;">确定并关闭</button>
            `;
            document.body.appendChild(panel);

            var btn = document.createElement('button');
            btn.id = 'eink-btn';
            btn.innerText = '⚙ 排版';
            btn.onclick = function() {
                panel.style.display = panel.style.display === 'block' ? 'none' : 'block';
            };
            document.body.appendChild(btn);

            document.getElementById('eink-fs').oninput = function(e) { localStorage.setItem('eink_fs', e.target.value); applySettings(); };
            document.getElementById('eink-lh').oninput = function(e) { localStorage.setItem('eink_lh', e.target.value); applySettings(); };
            document.getElementById('eink-ps').oninput = function(e) { localStorage.setItem('eink_ps', e.target.value); applySettings(); };
            document.getElementById('eink-pd').oninput = function(e) { localStorage.setItem('eink_pd', e.target.value); applySettings(); };
            document.getElementById('eink-close').onclick = function() { panel.style.display = 'none'; };

            var leftZone = document.createElement('div');
            leftZone.className = 'eink-zone-left';
            leftZone.onclick = function(e) {
                if (panel.style.display === 'block') return;
                window.scrollBy({ top: -window.innerHeight * 0.88, behavior: 'instant' });
            };

            var rightZone = document.createElement('div');
            rightZone.className = 'eink-zone-right';
            rightZone.onclick = function(e) {
                if (panel.style.display === 'block') return;
                window.scrollBy({ top: window.innerHeight * 0.88, behavior: 'instant' });
            };

            document.body.appendChild(leftZone);
            document.body.appendChild(rightZone);
        })();
        """.trimIndent()
        
        view?.evaluateJavascript(js, null)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
