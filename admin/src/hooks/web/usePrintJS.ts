import { nextTick } from 'vue';
import $printJS, { Configuration } from 'print-js';

export function printJS(configuration: Configuration) {
  if (configuration?.type === 'html') {
    printNb(configuration.printable);
  } else {
    return $printJS(configuration);
  }
}

export function printNb(domId?: string) {
  if (domId) {
    localPrint(domId);
  } else {
    window.print();
  }
}

let closeBtn = true;

function localPrint(domId: string) {
  const selector = typeof domId === 'string' && !domId.startsWith('#') ? '#' + domId : domId;
  nextTick(() => {
    if (!closeBtn) return;
    closeBtn = false;
    const el = document.querySelector(selector);
    if (!el) {
      closeBtn = true;
      window.print();
      return;
    }
    const iframe = document.createElement('iframe');
    iframe.style.position = 'fixed';
    iframe.style.right = '0';
    iframe.style.bottom = '0';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = '0';
    document.body.appendChild(iframe);
    const doc = iframe.contentWindow?.document;
    if (doc) {
      doc.open();
      const styles = Array.from(document.querySelectorAll('style, link[rel="stylesheet"]'))
        .map((node) => node.outerHTML)
        .join('\n');
      doc.write(`<!DOCTYPE html><html><head>${styles}</head><body>${el.outerHTML}</body></html>`);
      doc.close();
      iframe.onload = () => {
        iframe.contentWindow?.focus();
        iframe.contentWindow?.print();
        setTimeout(() => {
          document.body.removeChild(iframe);
          closeBtn = true;
        }, 500);
      };
    } else {
      document.body.removeChild(iframe);
      closeBtn = true;
    }
  });
}
