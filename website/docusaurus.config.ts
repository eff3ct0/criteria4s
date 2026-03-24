import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Criteria4s',
  tagline: 'Type-safe, data-store-agnostic criteria expressions for Scala 3',
  favicon: 'img/favicon.ico',

  future: {
    v4: true,
  },

  url: 'https://eff3ct0.github.io',
  baseUrl: '/criteria4s/',

  organizationName: 'eff3ct0',
  projectName: 'criteria4s',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

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
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Criteria4s',
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
      style: 'dark',
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
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'bash'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
