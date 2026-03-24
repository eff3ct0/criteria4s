import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Rafael Fernández',
  tagline: 'Type-safe, data-store-agnostic criteria expressions for Scala 3',
  favicon: 'img/r-favicon.png',

  future: {
    v4: true,
  },

  url: 'https://criteria4s.rafaelfernandez.dev',
  baseUrl: '/',

  organizationName: 'eff3ct0',
  projectName: 'criteria4s',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  stylesheets: [
    {
      href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;450;500;600;700&display=swap',
      type: 'text/css',
    },
  ],

  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          editUrl:
            'https://github.com/eff3ct0/criteria4s/edit/master/website/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Criteria4s',
      logo: {
        alt: 'Rafael Fernandez logo',
        src: 'img/r-logo.png',
        srcDark: 'img/r-logo-dark.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/eff3ct0/criteria4s',
          label: 'GitHub',
          position: 'right',
        },
        {
          href: 'https://central.sonatype.com/namespace/com.eff3ct',
          label: 'Maven Central',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'light',
      links: [
        {
          title: 'Resources',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/eff3ct0/criteria4s',
            },
            {
              label: 'MIT License',
              href: 'https://github.com/eff3ct0/criteria4s/blob/master/LICENSE',
            },
          ],
        },
      ],
      copyright: `Copyright © 2024-2026 Rafael Fernandez. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.vsDark,
      additionalLanguages: ['java', 'bash', 'scala'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
